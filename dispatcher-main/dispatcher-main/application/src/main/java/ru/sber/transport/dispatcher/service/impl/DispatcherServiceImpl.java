package ru.sber.transport.dispatcher.service.impl;

import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.dto.HasName;
import ru.sber.transport.dispatcher.dto.NewDispatcherDto;
import ru.sber.transport.dispatcher.dto.Projection;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DispatcherSearchDto;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.exceptions.ValidationException;
import ru.sber.transport.dispatcher.mappers.DispatcherMapper;
import ru.sber.transport.dispatcher.messaging.senders.ConfirmationSender;
import ru.sber.transport.dispatcher.messaging.senders.DispatcherSender;
import ru.sber.transport.dispatcher.service.AutoparkService;
import ru.sber.transport.dispatcher.service.ContractorCounter;
import ru.sber.transport.dispatcher.service.DispatcherService;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.request.Direction;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY_FORMATTER;
import static ru.sber.transport.dispatcher.database.model.Dispatcher_.expiryDate;
import static ru.sber.transport.dispatcher.dto.enums.PatchField.ATTORNEY_NUMBER;
import static ru.sber.transport.dispatcher.exceptions.ConflictException.DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE;

/**
 * Реализация сервиса работы с диспетчером.
 */
@Transactional
@RequiredArgsConstructor
@Component
class DispatcherServiceImpl implements DispatcherService {

    private final DispatcherRepository dispatcherRepository;

    private final ContractorRepository contractorRepository;

    private final DispatcherMapper mapper;

    private final DispatcherSender dispatcherSender;

    private final ContractorCounter contractorCounter;

    private final ConfirmationSender confirmationSender;

    private final AutoparkService autoparkService;

    @Override
    public Dispatcher add(@NonNull UUID contractorId, @NonNull NewDispatcherDto dispatcherData) {
        var contractor = contractorRepository.getReferenceById(contractorId);
        if (!contractorCounter.canAddStaff(contractor)) {
            throw new DuplicateDataException(Contractor.class, Map.of("id", contractorId, "employeeCount", contractor.getEmployeeCount()));
        }

        var exists = dispatcherRepository.findByPhoneAndActive(dispatcherData.phone(), true);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Dispatcher.class, Map.of("id", exists.get().getId(), "phone", dispatcherData.phone()));
        }

        exists = dispatcherRepository.findByEmailAndActive(dispatcherData.email(), true);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Dispatcher.class, Map.of("id", exists.get().getId(), "email", dispatcherData.email()));
        }

        var dispatcher = new Dispatcher();
        validateEwbCreationRequirements(
                dispatcherData.ewbCreationPossibility(),
                dispatcherData.attorneyNumber(),
                dispatcherData.issueDate(),
                dispatcherData.expiryDate(),
                dispatcherData.creationSystem());
        mapper.update(dispatcher, dispatcherData);

        dispatcher.setConsent(false);
        dispatcher.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.DS,
                contractor.getDigitId(),
                dispatcherRepository.countAllByContractorId(contractorId)+1));
        dispatcher.setContractor(contractor);
        dispatcher.setAutopark(autoparkService.get(dispatcherData.autoparkId()));

        setAttorneyNumber(dispatcherData.attorneyNumber(), dispatcher);
        var saved = dispatcherRepository.save(dispatcher);

        dispatcherSender.send(saved);

        contractorCounter.changeCount(contractor, 1);

        return saved;
    }

    @Override
    public Dispatcher edit(@NonNull UUID contractorId, @NonNull UUID dispatcherId, @NonNull NewDispatcherDto dispatcherData) {
        var dispatcher = dispatcherRepository.findByContractorIdAndId(contractorId, dispatcherId)
                .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, dispatcherId));

        var exists = dispatcherRepository.findByPhoneAndActiveTrueAndIdNot(dispatcherData.phone(), dispatcherId);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Dispatcher.class, Map.of("id", exists.get().getId(), "phone", dispatcherData.phone()));
        }

        exists = dispatcherRepository.findByEmailAndActiveTrueAndIdNot(dispatcherData.email(), dispatcherId);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Dispatcher.class, Map.of("id", exists.get().getId(), "email", dispatcherData.email()));
        }

        mapper.update(dispatcher, dispatcherData);
        dispatcher.setAutopark(autoparkService.get(dispatcherData.autoparkId()));
        var saved = dispatcherRepository.save(dispatcher);
        dispatcherSender.send(saved);
        return saved;
    }

    @Override
    public void delete(UUID contractorId) {
        var contractor = contractorRepository.findById(contractorId).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        var mainDispatcher = contractor.getMainDispatcher();
        if (mainDispatcher != null) {
            delete(contractorId, mainDispatcher.getId());
        }
    }

    @Override
    public void patchDispatcher(UUID id, Map<PatchField, Serializable> data) {
        var dispatcher = dispatcherRepository.findById(id)
                .orElseGet(() -> dispatcherRepository.findByOauthId(id).orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, id)));
        var phoneChanged = false;
        for (final var entry : data.entrySet()) {
            final var field = entry.getKey();
            switch (field) {
                case PHONE -> {
                    dispatcher.setPhone(String.valueOf(entry.getValue()));
                    dispatcher.setPhoneConfirmed(false);
                    phoneChanged = true;
                }
                case CONSENT -> dispatcher.setConsent(Boolean.parseBoolean(String.valueOf(entry.getValue())));
                case EWB_CREATION_POSSIBILITY -> patchEwbCreationRequirements(
                        dispatcher,
                        Objects.nonNull(entry.getValue()) && Boolean.parseBoolean(String.valueOf(entry.getValue())),
                        data); // процессит остальные переменные
                case AUTOPARK_ID -> dispatcher.setAutopark(autoparkService.get(Objects.nonNull(entry.getValue()) ? UUID.fromString(String.valueOf(entry.getValue())) : null));
            }
        }
        dispatcher = dispatcherRepository.save(dispatcher);
        if (phoneChanged){
            confirmationSender.send(dispatcher.getId(), dispatcher.getPhone());
        }
        dispatcherSender.send(dispatcher);
    }

    @Override
    public void delete(@NonNull UUID contractorId, @NonNull UUID dispatcherId) {
        dispatcherRepository.findByContractorIdAndId(contractorId, dispatcherId)
                .ifPresent(dispatcher -> {
                    var deactivate = new LinkedHashSet<>(Set.of(dispatcher));

                    var contractor = dispatcher.getContractor();
                    if (Objects.equals(Optional.ofNullable(contractor.getMainDispatcher()).map(Dispatcher::getId).orElse(null), dispatcher.getId())) {
                        contractor.setMainDispatcher(null);
                        deactivate.addAll(dispatcherRepository.findAllByContractorId(contractorId));
                    }
                    deactivate.forEach(d -> {
                        d.setActive(false);
                        d.setOauthId(null);
                    });
                    dispatcherRepository.saveAll(deactivate).forEach(dispatcherSender::send);
                    contractorRepository.save(contractor);
                    contractorCounter.changeCount(contractor, -1 * deactivate.size());
                });
    }

    @Override
    public Optional<Dispatcher> get(@NonNull UUID contractorId, @NonNull UUID dispatcherId) {
        return dispatcherRepository.findByContractorIdAndId(contractorId, dispatcherId);
    }

    @Override
    public Optional<Dispatcher> findByOauthIdAndContractorId(UUID oauthId, UUID contractorId) {
        return dispatcherRepository.findByOauthIdAndContractorId(oauthId, contractorId);
    }

    @Override
    public Optional<Dispatcher> get(@NonNull UUID dispatcherId) {
        return dispatcherRepository.findById(dispatcherId);
    }

    @Override
    public Optional<Dispatcher> getByOauthId(@NonNull UUID oauthId) {
        return dispatcherRepository.findByOauthId(oauthId);
    }

    @Override
    public void signPdn(@NonNull UUID dispatcherId) {
        var dispatcher = dispatcherRepository.findById(dispatcherId)
                .orElseGet(() -> dispatcherRepository.findByOauthId(dispatcherId)
                        .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, dispatcherId)));

        dispatcher.setConsent(true);

        dispatcher = dispatcherRepository.save(dispatcher);
        dispatcherSender.send(dispatcher);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Iterable<HasName> get(@NonNull UUID contractorId, DispatcherSearchDto search, Projection projection) {
        Pageable pageable;
        if (search != null && Projection.FULL.equals(projection)) {
            var sort = getSort(search);
            pageable = PageRequest.of(search.getPage(), search.getSize(), sort);
        } else {
            pageable = Pageable.unpaged();
        }
        var spec = getSpec(contractorId, search);
        if (Projection.SELECT.equals(projection)) {
            return dispatcherRepository.findAll(spec, pageable).stream().<HasName>map(mapper::toSelectDto).toList();
        }
        return dispatcherRepository.findAll(spec, pageable).map(mapper::toDto);
    }

    private Specification<Dispatcher> getSpec(UUID contractorId, DispatcherSearchDto dispatcherSearchDto) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.equal(root.get(Dispatcher_.contractor).get(Contractor_.id), contractorId);
            if (dispatcherSearchDto == null) {
                return predicate;
            }
            for (var entry : dispatcherSearchDto.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case FIRST_NAME -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(Dispatcher_.FIRST_NAME)),
                                        value.toString().toLowerCase(Locale.ROOT)));
                        case LAST_NAME -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(Dispatcher_.LAST_NAME)),
                                        value.toString().toLowerCase(Locale.ROOT)));
                        case PATRONYMIC -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(Dispatcher_.PATRONYMIC)),
                                        value.toString().toLowerCase(Locale.ROOT)));
                        case ACTIVE -> criteriaBuilder.and(predicate,
                                criteriaBuilder.equal(root.get(Dispatcher_.ACTIVE), value));
                        case AUTOPARK_ID -> criteriaBuilder.and(predicate,
                                criteriaBuilder.equal(root.get(Dispatcher_.autopark).get(Autopark_.id), value));
                    };
                }
            }
            query.distinct(true);
            return predicate;
        };
    }

    /**
     * Пропатчить требования к созданию ЭПД.
     * @param dispatcher диспетчер
     * @param ewbCreationPossibility возможность создания ЭПД
     * @param data данные
     */
    private void patchEwbCreationRequirements(Dispatcher dispatcher, boolean ewbCreationPossibility, Map<PatchField, Serializable> data) {
        dispatcher.setEwbCreationPossibility(ewbCreationPossibility);
        if (Boolean.FALSE.equals(ewbCreationPossibility)) {
            dispatcher.setIssueDate(null);
            dispatcher.setAttorneyNumber(null);
            dispatcher.setExpiryDate(null);
            dispatcher.setCreationSystem(null);
            return;
        }

        String attorneyNumber = (String) data.getOrDefault(ATTORNEY_NUMBER, null);
        String issueDateStr = (String) data.getOrDefault(PatchField.ISSUE_DATE, null);
        LocalDate issueDate = issueDateStr != null ? LocalDate.parse(issueDateStr, DDMMYYYY_FORMATTER) : null;
        String expiryDateStr = (String) data.getOrDefault(PatchField.EXPIRY_DATE, null);
        LocalDate expiryDate = expiryDateStr != null ? LocalDate.parse(expiryDateStr, DDMMYYYY_FORMATTER) : null;
        String creationSystem = (String) data.getOrDefault(PatchField.CREATION_SYSTEM, null);

        validateEwbCreationRequirements(ewbCreationPossibility, attorneyNumber, issueDate, expiryDate, creationSystem);
        setAttorneyNumber(attorneyNumber, dispatcher);
        dispatcher.setIssueDate(issueDate);
        dispatcher.setExpiryDate(expiryDate);
        dispatcher.setCreationSystem(creationSystem);
    }

    /**
     * Установка номера доверенности.
     * @param attorneyNumber номер доверенности
     * @param dispatcher диспетчер
     */
    private void setAttorneyNumber(String attorneyNumber, Dispatcher dispatcher) {
        if (StringUtils.isBlank(attorneyNumber)) return;
        Optional<Dispatcher> byAttorneyNumber = dispatcherRepository.findByAttorneyNumberAndActive(attorneyNumber, true);
        if (byAttorneyNumber.isPresent() && !Objects.equals(byAttorneyNumber.get().getId(), dispatcher.getId())) {
            throw new ConflictException(String.format(DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, attorneyNumber));
        } else {
            dispatcher.setAttorneyNumber(attorneyNumber);
        }
    }

    /**
     * Валидация требований к созданию ЭПД.
     * @param ewbCreationPossibility Флаг возможности создания ЭПЛ
     * @param attorneyNumber номер доверенности
     * @param issueDate Дата выдачи
     * @param expiryDate Дата окончания срока действия
     * @param creationSystem Система создания
     */
    private void validateEwbCreationRequirements(Boolean ewbCreationPossibility,
                                                 String attorneyNumber,
                                                 LocalDate issueDate,
                                                 LocalDate expiryDate,
                                                 String creationSystem) {
        if (Boolean.TRUE.equals(ewbCreationPossibility)) {
            if (StringUtils.isEmpty(attorneyNumber)) {
                throw new ValidationException("Поле 'attorneyNumber' обязательно, если разрешено создание ЭПЛ");
            }
            if (Objects.isNull(issueDate)) {
                throw new ValidationException("Поле 'issueDate' обязательно, если разрешено создание ЭПЛ");
            }
            if (Objects.isNull(expiryDate)) {
                throw new ValidationException("Поле 'expiryDate' обязательно, если разрешено создание ЭПЛ");
            }
            if (StringUtils.isEmpty(creationSystem)) {
                throw new ValidationException("Поле 'creationSystem' обязательно, если разрешено создание ЭПЛ");
            }
        }
    }

    private Sort getSort(DispatcherSearchDto searchDto) {
        var sort = switch (searchDto.getField()) {
            case FIRST_NAME -> Sort.sort(Dispatcher.class).by(Dispatcher::getFirstName);
            case LAST_NAME -> Sort.sort(Dispatcher.class).by(Dispatcher::getLastName);
            case PATRONYMIC -> Sort.sort(Dispatcher.class).by(Dispatcher::getPatronymic);
            case ACTIVE -> Sort.sort(Dispatcher.class).by(Dispatcher::isActive);
            case AUTOPARK_ID -> Sort.sort(Dispatcher.class).by(Dispatcher::getAutopark);
        };
        return Direction.ASC.equals(searchDto.getDirection()) ? sort.ascending() : sort.descending();
    }
}
