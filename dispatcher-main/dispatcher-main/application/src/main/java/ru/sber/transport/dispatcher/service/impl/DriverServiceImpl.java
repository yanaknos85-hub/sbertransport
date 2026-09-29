package ru.sber.transport.dispatcher.service.impl;

import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.DriverLicenseDto;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;
import ru.sber.transport.dispatcher.mappers.DriverMapper;
import ru.sber.transport.dispatcher.mappers.VehicleMapper;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.ConfirmationSender;
import ru.sber.transport.dispatcher.messaging.senders.DriverSender;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.service.*;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY_FORMATTER;

/**
 * Implementation of driver controller service.
 */
@RequiredArgsConstructor
@Slf4j
@Transactional
@Component
class DriverServiceImpl implements DriverService {

    private final ContractorService contractorService;

    private final DriverRepository driverRepository;

    private final DriverMapper mapper;

    private final ShiftService shiftService;

    private final VehicleMapper vehicleMapper;

    private final ContractorCounter contractorCounter;

    private final DriverSender driverSender;

    private final VerificationService verificationService;

    private final ConfirmationSender confirmationSender;

    private final AutoparkService autoparkService;

    private final static String LIKE_FORMAT_STRING = "%%%s%%";

    @Override
    public DriverDTO add(UUID contractorId, NewDriverDTO driverDTO) {
        var contractor = contractorService.get(contractorId).
                orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        if (!contractorCounter.canAddStaff(contractor)) {
            throw new DuplicateDataException(Contractor.class, Map.of("id", contractorId, "employeeCount", contractor.getEmployeeCount()));
        }

        var driver = new Driver();

        validateDriverData(driverDTO, null);
        mapper.update(driver, driverDTO);
        driver.setConsent(false);
        driver.setContractor(contractor);
        driver.setHumanReadableId(getHumanReadable(contractor));
        driver.setAutopark(autoparkService.get(driverDTO.autoparkId()));

        driver = driverRepository.save(driver);
        if (driver.isActive()) {
            contractorCounter.changeCount(contractor, 1);
        }
        driverSender.send(driver, Source.CONTRACTOR, true);
        return mapper.toDto(driver);
    }

    @Override
    public void edit(UUID contractorId, UUID driverId, NewDriverDTO driverDTO) {
        var contractor = contractorService.get(contractorId).
                orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        var driver = driverRepository.findByContractorAndId(contractorId, driverId).
                orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        validateDriverData(driverDTO, driverId);
        var oldActive = driver.isActive();
        Boolean toggle = null;
        if (!driverDTO.active() && oldActive) {
            toggle = false;
        } else if (!oldActive && driverDTO.active()) {
            if (!contractorCounter.canAddStaff(contractor)) {
                throw new DuplicateDataException(Contractor.class, Map.of("id", contractorId, "employeeCount", contractor.getEmployeeCount()));
            }
            toggle = true;
        }
        mapper.update(driver, driverDTO);
        driver.setAutopark(autoparkService.get(driverDTO.autoparkId()));
        driver.setContractor(contractor);
        if (driver.getAttributes() != null) {
            driver.getAttributes().forEach(a -> a.setContractor(contractor));
        }

        var saved = driverRepository.save(driver);
        driverSender.send(saved, Source.CONTRACTOR, true);

        if (toggle != null) {
            if (Boolean.TRUE.equals(toggle)) {
                contractorCounter.changeCount(contractor, 1);
            } else {
                contractorCounter.changeCount(contractor, -1);
            }
        }
    }

    @Override
    public void delete(UUID contractorId, UUID driverId) {
        var driver = driverRepository.findByContractorAndId(contractorId, driverId).
                orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        driver.setActive(false);
        driver.setOauthId(null);
        driver = driverRepository.save(driver);
        driverSender.send(driver, Source.CONTRACTOR, true);
    }

    @Override
    public void deleteAllByContractorId(UUID contractorId) {
        var drivers = driverRepository.findAllByContractorId(contractorId);
        drivers.forEach(driver -> {
            driver.setActive(false);
            driver.setOauthId(null);
        });
        driverRepository.saveAll(drivers);
        driverSender.sendAll(drivers);
    }

    @Override
    public Optional<Driver> get(UUID driverId) {
        return driverRepository.findById(driverId);
    }

    @Override
    public void save(Driver driver) {
        driverRepository.save(driver);
    }

    @Override
    public DriverDTO get(UUID contractorId, UUID driverId) {
        var exists = contractorService.isContractorExists(contractorId);
        if (!exists) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        return driverRepository.findByContractorAndId(contractorId, driverId).
                map(mapper::toDto).
                orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
    }

    @Override
    public Page<DriverDTO> get(UUID contractorId, DriverSearchDTO searchDTO) {
        var sort = getSort(searchDTO);
        var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        return driverRepository.findAll(createSpec(contractorId, searchDTO), pageable).map(mapper::toDto);
    }

    private Specification<Driver> createSpec(@NonNull UUID contractorId, DriverSearchDTO searchDTO) {
        var filter = searchDTO.getFilter();
        return (root, q, cb) -> {
            var contractor = root.join(Driver_.contractor);
            var predicate = cb.equal(contractor.get(Contractor_.id), contractorId);
            for (var entry : filter.entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case ACTIVE -> cb.and(predicate, cb.equal(root.get(Driver_.active), entry.getValue()));
                        case RATING -> cb.and(predicate, cb.greaterThanOrEqualTo(root.get(Driver_.rating), Integer.valueOf(String.valueOf(entry.getValue()))));
                        case DRIVER_HUMAN_ID -> cb.and(predicate, cb.like(root.get(Driver_.humanReadableId), LIKE_FORMAT_STRING.formatted(entry.getValue())));
                        case DRIVER_FULL_NAME -> cb.and(predicate, appendFullNameFilter(predicate, cb, root, String.valueOf(entry.getValue())));
                        case DRIVER_LICENSES -> appendLicensePredicate(predicate, cb, root, ReflectionUtils.castObjectToList(entry.getValue(), DriverLicenseDto.class));
                        case DRIVER_SPECIALITY -> cb.and(predicate, cb.equal(root.get(Driver_.driverSpeciality), entry.getValue()));
                        case AUTOPARK_ID -> cb.and(predicate, cb.equal(root.get(Driver_.autopark).get(Autopark_.id), entry.getValue()));
                        case PERSONNEL_NUMBER -> cb.and(predicate, cb.like(root.get(Driver_.personnelNumber), LIKE_FORMAT_STRING.formatted(entry.getValue())));
                    };
                }
            }
            q.distinct(true);
            return predicate;
        };
    }

    private Predicate appendLicensePredicate(Predicate predicate, CriteriaBuilder cb, Root<Driver> root, List<DriverLicenseDto> value) {
        var licenses = root.join(Driver_.driverLicenses);
        return cb.and(predicate, licenses.in(value.stream().map(DriverLicenseDto::name).map(DriverLicense::valueOf).collect(Collectors.toUnmodifiableSet())));
    }

    /**
     * Добавляет к предикату поиск по всем возможным комбинациям имени.
     *
     * @param predicate       исходный предикат.
     * @param criteriaBuilder построитель запросов.
     * @param root            корневой элемент запроса.
     * @param value           фильтруемое значение.
     * @return предикат.
     */
    private Predicate appendFullNameFilter(Predicate predicate, CriteriaBuilder criteriaBuilder, Root<Driver> root, String value) {
        var lastNameExtractor = criteriaBuilder.lower(root.get(Driver_.lastName));
        var firstNameExtractor = criteriaBuilder.lower(root.get(Driver_.firstName));
        var patronymicExtractor = criteriaBuilder.lower(root.get(Driver_.patronymic));
        var extendedValue = "%%%s%%".formatted(value.toLowerCase(Locale.ROOT));

        var fullNameLikePredicate = switch (value.split(" ").length) {
            case 1 ->
                    simpleNamePredicate(criteriaBuilder, extendedValue, lastNameExtractor, firstNameExtractor, patronymicExtractor);
            case 2 ->
                    twoNamePredicate(criteriaBuilder, extendedValue, lastNameExtractor, firstNameExtractor, patronymicExtractor);
            default ->
                    fullNamePredicate(criteriaBuilder, extendedValue, lastNameExtractor, firstNameExtractor, patronymicExtractor);
        };

        return criteriaBuilder.and(predicate, fullNameLikePredicate);
    }

    /**
     * Добавляет к предикату поиск по ФИО или ИФО.
     *
     * @param criteriaBuilder     построитель запросов.
     * @param value               фильтруемое значение.
     * @param firstNameExtractor  объект с данными имени.
     * @param lastNameExtractor   объект с данными фамилии.
     * @param patronymicExtractor объект с данными отчества.
     * @return предикат.
     */
    private Predicate fullNamePredicate(
            CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
            Expression<String> patronymicExtractor
    ) {
        var lastFirstPatronymic = criteriaBuilder.concat(criteriaBuilder.concat(lastNameExtractor, criteriaBuilder.concat(" ", firstNameExtractor)), criteriaBuilder.concat(" ", patronymicExtractor));
        var firstLastPatronymic = criteriaBuilder.concat(criteriaBuilder.concat(firstNameExtractor, criteriaBuilder.concat(" ", lastNameExtractor)), criteriaBuilder.concat(" ", patronymicExtractor));
        return criteriaBuilder.or(criteriaBuilder.like(lastFirstPatronymic, value),
                criteriaBuilder.like(firstLastPatronymic, value));
    }

    /**
     * Добавляет к предикату поиск по ИО, ИФ или ФИ.
     *
     * @param criteriaBuilder     построитель запросов.
     * @param value               фильтруемое значение.
     * @param firstNameExtractor  объект с данными имени.
     * @param lastNameExtractor   объект с данными фамилии.
     * @param patronymicExtractor объект с данными отчества.
     * @return предикат.
     */
    private Predicate twoNamePredicate(
            CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
            Expression<String> patronymicExtractor
    ) {
        var lastFirstName = criteriaBuilder.concat(lastNameExtractor, criteriaBuilder.concat(" ", firstNameExtractor));
        var firstLastName = criteriaBuilder.concat(firstNameExtractor, criteriaBuilder.concat(" ", lastNameExtractor));
        var firstPatronymicName = criteriaBuilder.concat(firstNameExtractor, criteriaBuilder.concat(" ", patronymicExtractor));
        return criteriaBuilder.or(criteriaBuilder.like(lastFirstName, value),
                criteriaBuilder.like(firstLastName, value),
                criteriaBuilder.like(firstPatronymicName, value));
    }

    /**
     * Добавляет к предикату поиск по И, Ф или О.
     *
     * @param criteriaBuilder     построитель запросов.
     * @param value               фильтруемое значение.
     * @param firstNameExtractor  объект с данными имени.
     * @param lastNameExtractor   объект с данными фамилии.
     * @param patronymicExtractor объект с данными отчества.
     * @return предикат.
     */
    private Predicate simpleNamePredicate(
            CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
            Expression<String> patronymicExtractor
    ) {
        return criteriaBuilder.or(criteriaBuilder.like(lastNameExtractor, value),
                criteriaBuilder.like(firstNameExtractor, value),
                criteriaBuilder.like(patronymicExtractor, value));
    }

    private String getHumanReadable(Contractor contractor) {
        return "%s-%04d-%08d".formatted(Prefix.DR,
                contractor.getDigitId(),
                driverRepository.countAllByContractorId(contractor.getId())+1);
    }

    private Sort getSort(DriverSearchDTO driverSearchDTO) {
        var sort = switch (driverSearchDTO.getField()) {
            case RATING -> Sort.sort(Driver.class).by(Driver::getRating);
            case DRIVER_HUMAN_ID -> Sort.sort(Driver.class).by(Driver::getHumanReadableId);
            case ACTIVE -> Sort.sort(Driver.class).by(Driver::isActive);
            default -> getFullNameSort();
        };
        return Direction.ASC.equals(driverSearchDTO.getDirection()) ? sort.ascending() : sort.descending();
    }

    private Sort getFullNameSort() {
        var driver = Sort.sort(Driver.class);
        var sortLastName = Sort.sort(Driver.class).by(Driver::getLastName);
        var sortFirstName = Sort.sort(Driver.class).by(Driver::getFirstName);
        var sortPatronymic = Sort.sort(Driver.class).by(Driver::getPatronymic);

        return driver.and(sortLastName).and(sortFirstName).and(sortPatronymic);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public VehicleDTO getSelfVehicle(Driver driver) {
        var shifts = shiftService.getShiftByDriverIdAndCurrentDate(driver, LocalDateTime.now(ZoneOffset.UTC))
                .stream().sorted(Comparator.comparing(Shift::getStartDate)).toList();
        verificationService.checkShiftsExistence(shifts);
        return vehicleMapper.toDto(shifts.getFirst().getVehicle(), shifts.getFirst().getEwbId());
    }

    @Override
    public void signPdn(@NonNull UUID driverId) {
        var driver = driverRepository.findById(driverId)
                .orElseGet(() -> driverRepository.findByOauthId(driverId)
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId)));

        driver.setConsent(true);

        driver = driverRepository.save(driver);
        driverSender.send(driver, Source.CONTRACTOR, false);
    }

    @Override
    public void patchDriver(UUID id, Map<PatchField, Serializable> data) {
        var driver = driverRepository.findById(id)
                .orElseGet(() -> driverRepository.findByOauthId(id)
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, id)));
        var phoneChanged = false;
        for (final var entry : data.entrySet()) {
            final var field = entry.getKey();
            switch (field) {
                case PHONE -> {
                    driver.setContactPhone(String.valueOf(entry.getValue()));
                    driver.setPhoneConfirmed(false);
                    phoneChanged = true;
                }
                case CONSENT -> driver.setConsent(Boolean.parseBoolean(String.valueOf(entry.getValue())));
                case TIN -> driver.setTin(Objects.nonNull(entry.getValue()) ? String.valueOf(entry.getValue()) : null);
                case SNILS -> driver.setSnils(Objects.nonNull(entry.getValue()) ? String.valueOf(entry.getValue()) : null);
                case DRIVER_LICENSE_NUMBER -> driver.setDriverLicenseNumber(Objects.nonNull(entry.getValue()) ? String.valueOf(entry.getValue()) : null);
                case ISSUE_DATE -> driver.setIssueDate(Objects.nonNull(entry.getValue()) ? LocalDate.parse(String.valueOf(entry.getValue()), DDMMYYYY_FORMATTER) : null);
                case EXPIRY_DATE -> driver.setExpiryDate(Objects.nonNull(entry.getValue()) ? LocalDate.parse(String.valueOf(entry.getValue()), DDMMYYYY_FORMATTER) : null);
                case DRIVER_LICENSES -> {
                    Set<DriverLicense> licenses = new HashSet<>();
                    List<String> licensesStr = Objects.nonNull(entry.getValue()) ? (List<String>) entry.getValue() : null;
                    if (licensesStr != null) {
                        licensesStr.forEach(e -> {
                            licenses.add(DriverLicense.valueOf(e));
                        });
                    }
                    driver.setDriverLicenses(licenses);
                }
                case AUTOPARK_ID -> driver.setAutopark(Objects.nonNull(entry.getValue()) ? autoparkService.get(UUID.fromString(String.valueOf(entry.getValue()))) : null);
                case SPECIALITY -> driver.setDriverSpeciality(Objects.nonNull(entry.getValue()) ? DriverSpecialityType.valueOf(String.valueOf(entry.getValue())) : null);
            }
        }
        driver = driverRepository.save(driver);
        if(phoneChanged){
            confirmationSender.send(driver.getId(), driver.getContactPhone());
        }
        driverSender.send(driver, Source.CONTRACTOR, true);
    }

    @Override
    public Optional<Driver> findByOauthId(UUID id) {
        return driverRepository.findByOauthId(id);
    }

    public void validateDriverData(NewDriverDTO driver, UUID driverId){
        if(driver.passport() != null &&
                (driverId == null ?
                        driverRepository.existsByPassportAndActiveTrue(driver.passport())
                        : driverRepository.existsByPassportAndActiveTrueAndIdNot(driver.passport(), driverId))) {
            throw new DuplicateDataException(Driver.class, Map.of("passport", driver.passport(), "active", true));
        }
        if(driver.driverLicenseNumber() != null &&
                (driverId == null ?
                        driverRepository.existsByDriverLicenseNumberAndActiveTrue(driver.driverLicenseNumber())
                        : driverRepository.existsByDriverLicenseNumberAndActiveTrueAndIdNot(driver.driverLicenseNumber(), driverId))) {
            throw new DuplicateDataException(Driver.class, Map.of("driverLicenseNumber", driver.driverLicenseNumber(), "active", true));
        }
        if(driver.snils() != null &&
                (driverId == null ?
                        driverRepository.existsBySnilsAndActiveTrue(driver.snils())
                        : driverRepository.existsBySnilsAndActiveTrueAndIdNot(driver.snils(), driverId))) {
            throw new DuplicateDataException(Driver.class, Map.of("snils", driver.snils(), "active", true));
        }
        if(driver.tin() != null &&
                (driverId == null ?
                        driverRepository.existsByTinAndActiveTrue(driver.tin())
                        : driverRepository.existsByTinAndActiveTrueAndIdNot(driver.tin(), driverId))) {
            throw new DuplicateDataException(Driver.class, Map.of("tin", driver.tin(), "active", true));
        }
    }
}
