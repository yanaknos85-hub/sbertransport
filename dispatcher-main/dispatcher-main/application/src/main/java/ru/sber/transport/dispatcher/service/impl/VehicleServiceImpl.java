package ru.sber.transport.dispatcher.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.dao.TripsRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.enums.ResponseFields;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;
import ru.sber.transport.dispatcher.dto.search.TransportSearchParameters;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.mappers.VehicleMapper;
import ru.sber.transport.dispatcher.messages.TransportMessage;
import ru.sber.transport.dispatcher.messaging.senders.VehicleSender;
import ru.sber.transport.dispatcher.service.VehicleService;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.request.Direction;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Implementation of service for working with transport.
 */
@RequiredArgsConstructor
@Transactional
@Component
class VehicleServiceImpl implements VehicleService {

    private static final String LIKE_FORMAT = "%%%s%%";
    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepository vehicleRepository;

    private final AutoparkRepository autoparkRepository;

    private final VehicleMapper mapper;

    private final VehicleSender vehicleSender;

    private final ShiftRepository shiftRepository;

    private final EntityManager entityManager;

    private final TripsRepository tripsRepository;

    @Override
    public VehicleDTO add(UUID contractorId, UUID autoparkId, NewVehicleDTO vehicleDTO) {
        var stateNumber = vehicleDTO.getStateNumber();
        var vin = vehicleDTO.getVin();
        var passport = vehicleDTO.getPassport();

        var exists = vehicleRepository.findByStateNumberOrVinOrPassport(stateNumber, vin, passport);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Vehicle.class, Map.of("id", exists.get().getId(),
                    "stateNumber", stateNumber,
                    "vin", Objects.requireNonNullElse(vin, ""),
                    "passport", Objects.requireNonNullElse(passport, "")));
        }

        var autopark = autoparkRepository.findByContractorIdAndIdAndActiveIsTrue(contractorId, autoparkId)
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, autoparkId));

        var vehicle = new Vehicle();

        mapper.update(vehicle, vehicleDTO);
        vehicle.setAutopark(autopark);

        var saved = vehicleRepository.save(vehicle);
        vehicleSender.send(saved);
        return mapper.toDto(saved, null);
    }

    @Override
    public void edit(UUID contractorId, UUID autoparkId, UUID vehicleId, NewVehicleDTO vehicleDTO) {
        var stateNumber = vehicleDTO.getStateNumber();
        var vin = vehicleDTO.getVin();
        var passport = vehicleDTO.getPassport();

        var vehicle = vehicleRepository.findByAutoparkContractorIdAndAutoparkIdAndIdAndActiveTrue(contractorId, autoparkId, vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, vehicleId));

        var exists = vehicleRepository.findByStateNumberOrVinOrPassportExclude(stateNumber, vin, passport, vehicleId);
        if (exists.isPresent()) {
            throw new DuplicateDataException(Vehicle.class, Map.of("id", exists.get().getId(),
                    "stateNumber", stateNumber,
                    "vin", vin,
                    "passport", passport));
        }

        mapper.update(vehicle, vehicleDTO);

        if (vehicleDTO.getAutopark() != null) {
            var autopark = autoparkRepository.findByContractorIdAndIdAndActiveIsTrue(contractorId, vehicleDTO.getAutopark().id())
                    .orElseThrow(() -> new EntityNotFoundException(Autopark.class, vehicleDTO.getAutopark().id()));
            vehicle.setAutopark(autopark);
        }

        vehicle = vehicleRepository.save(vehicle);
        vehicleSender.send(vehicle);
    }

    @Override
    public void delete(UUID contractorId, UUID autoparkId, UUID vehicleId) {
        var vehicle = vehicleRepository.findByAutoparkContractorIdAndAutoparkIdAndIdAndActiveTrue(contractorId, autoparkId, vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, vehicleId));
        vehicleRepository.delete(vehicle);
    }

    @Override
    public void upsert(TransportMessage message) {
        if (message.autoparkId() != null) {
            var vehicle = vehicleRepository.findByTransportId(message.id()).orElse(new Vehicle());
            mapper.updateVehicleEntityWithUniversalType(vehicle, message);
            if(vehicle.getAutopark() == null) {
                var autopark = autoparkRepository.findById(message.autoparkId()).orElse(null);
                if (autopark == null) {
                    log.debug("Autopark with id {} not found, transport message can not be saved", message.autoparkId());
                    return;
                }
                vehicle.setAutopark(autopark);
            }
            var saved = vehicleRepository.save(vehicle);
            vehicleSender.send(saved);
        } else {
            log.debug("Autopark id is null, transport message can not be saved");
        }
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public VehicleDTO get(UUID contractorId, UUID autoparkId, UUID vehicleId) {
        return vehicleRepository.findByAutoparkContractorIdAndAutoparkIdAndIdAndActiveTrue(contractorId, autoparkId, vehicleId)
                .map(vehicle -> mapper.toDto(vehicle, null))
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, vehicleId));
    }

    @Override
    public List<Vehicle> getAllByAutopark(UUID autoparkId) {
        return vehicleRepository.findAllByAutoparkId(autoparkId);
    }

    @Override
    public Collection<Vehicle> getAllByAutoparkAndActiveTrueAndInExploitation(UUID autoparkId) {
        return vehicleRepository.findAllByAutoparkIdAndActiveTrueAndInExploitationTrue(autoparkId);
    }

    @Override
    public Page<VehicleDTO> getAllPageable(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO) {
        return (Page<VehicleDTO>) findAll(contractorId, autoparkId, searchDTO, true);
    }

    @Override
    public List<VehicleDTO> getAll(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO) {
        return (List<VehicleDTO>) findAll(contractorId, autoparkId, searchDTO, false);
    }

    @Override
    public Page<TransportDTO> getAllByContractorAndFilters(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        decodeValues(transportSearchDTO);
        var sort = Sort.by(transportSearchDTO.getField().getName());
        var pageable = PageRequest.of(transportSearchDTO.getPage(), transportSearchDTO.getSize(), Direction.ASC.equals(transportSearchDTO.getDirection()) ? sort.ascending() : sort.descending());
        var vehicles = getFreeTransport(contractorId, transportSearchDTO);
        if (transportSearchDTO.getResult() != null
                && !transportSearchDTO.getResult().isEmpty()
                && transportSearchDTO.getStartDate() == null
                && transportSearchDTO.getEndDate() == null) {
            return new PageImpl<>(vehicles.stream()
                    .map(mapper::toTransportDto)
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .toList(),
                    pageable, vehicles.size());
        }
        var vehiclesIds = vehicles.parallelStream().map(Vehicle::getId).toList();
        var timeZone = ZoneOffset.of(URLDecoder.decode(transportSearchDTO.getTimeZone(), StandardCharsets.UTF_8));
        var startDate = transportSearchDTO.getStartDate() == null ? OffsetDateTime.of(LocalDate.EPOCH.atStartOfDay(), ZoneOffset.UTC) : transportSearchDTO.getStartDate();
        var endDate = transportSearchDTO.getEndDate() == null ? OffsetDateTime.of(LocalDate.EPOCH.atStartOfDay().plusYears(1000), ZoneOffset.UTC) : transportSearchDTO.getEndDate();
        var trips = tripsRepository.findAllByVehicleIdsAndDate(vehiclesIds, startDate, endDate);
        var transportList = new ArrayList<TransportDTO>();
        vehicles.forEach(vehicle -> {
            var vehicleTrips = new ArrayList<Trip>();
            trips.parallelStream().filter(trip -> trip.getVehicleId().equals(vehicle.getId())).forEach(vehicleTrips::add);
            var transportDto = mapper.toTransportDto(vehicle);
            transportDto.getTrips().addAll(vehicleTrips.parallelStream()
                    .map(trip -> new TransportDTO.Trip(
                            trip.getStartTime().withOffsetSameInstant(timeZone),
                            trip.getEndTime().withOffsetSameInstant(timeZone)))
                    .toList());
            transportList.add(transportDto);
        });
        return new PageImpl<>(transportList.parallelStream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList(),
                pageable, transportList.size());
    }

    @Override
    public Page<TransportDTO> getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        var sort = Sort.by(transportSearchDTO.getField().getName());
        var searchString = transportSearchDTO.getSearch() == null ? "" : transportSearchDTO.getSearch();
        var pageable = PageRequest.of(transportSearchDTO.getPage(), transportSearchDTO.getSize(), Direction.ASC.equals(transportSearchDTO.getDirection()) ? sort.ascending() : sort.descending());
        var vehicles = vehicleRepository.findAllFreeTransport(transportSearchDTO.getStartDate(), transportSearchDTO.getEndDate(), contractorId, VehicleType.PASSENGER, searchString);
        return new PageImpl<>(vehicles.stream()
                .map(mapper::toTransportDto)
                .sorted(Comparator.comparing(TransportDTO::getBrand))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList(),
                pageable, vehicles.size());
    }

    @Override
    public List<TransportDTO.Trip> getAllByContractorAndVehicleId(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO) {
        var timeZone = ZoneOffset.of(URLDecoder.decode(transportSearchDTO.getTimeZone(), StandardCharsets.UTF_8));
        var startDate = transportSearchDTO.getStartDate() == null ? OffsetDateTime.of(LocalDate.EPOCH.atStartOfDay(), ZoneOffset.UTC) : transportSearchDTO.getStartDate();
        var endDate = transportSearchDTO.getEndDate() == null ? OffsetDateTime.of(LocalDate.EPOCH.atStartOfDay().plusYears(1000), ZoneOffset.UTC) : transportSearchDTO.getEndDate();
        var trips = tripsRepository.findAllByVehicleIdsAndDate(List.of(vehicleId), startDate, endDate);
        return trips.parallelStream()
                .map(trip -> new TransportDTO.Trip(
                        trip.getStartTime().withOffsetSameInstant(timeZone),
                        trip.getEndTime().withOffsetSameInstant(timeZone)))
                .toList();

    }

    @Override
    public Page<VehicleShiftResponse> getAllByContractorIdAndFilters(UUID contractorId, VehicleShiftSearchDto searchDto) {
        var sort = Sort.by(searchDto.getField().getName(), Vehicle_.stateNumber.getName());
        var pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);
        Specification<Vehicle> spec = (root, query, cb) -> {
            var autopark = root.join(Vehicle_.autopark);
            var contractor = autopark.join(Autopark_.contractor);
            var predicate = cb.equal(contractor.get(Contractor_.id), contractorId);
            predicate = cb.and(predicate, cb.equal(root.get(Vehicle_.IN_EXPLOITATION), true));
            if (searchDto.getAutoparkId() != null) {
                predicate = cb.and(predicate, cb.equal(autopark.get(Autopark_.id), searchDto.getAutoparkId()));
            }
            if (searchDto.getSearch() != null && !searchDto.getSearch().isEmpty()) {
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get(Vehicle_.stateNumber)), cb.lower(cb.literal(LIKE_FORMAT.formatted(searchDto.getSearch())))),
                        cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.brand)), cb.lower(cb.literal(LIKE_FORMAT.formatted(searchDto.getSearch())))),
                        cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.name)), cb.lower(cb.literal(LIKE_FORMAT.formatted(searchDto.getSearch()))))
                ));
            }
            return predicate;
        };
        var vehicles = vehicleRepository.findAll(spec, pageable);
        var shifts = shiftRepository.findAllByVehicleIdsAndDate(vehicles.stream()
                .map(Vehicle::getId).toList(), searchDto.getStartDate(), searchDto.getEndDate());
        return vehicles.map(vehicle -> mapper.toVehicleShiftResponse(vehicle, shifts));
    }

    @Override
    public List<VehicleShiftStatusResponse> getVehicleShiftStatus(UUID contractorId, List<UUID> vehicleIds) {
        var list = shiftRepository.findAllByContractorIdAndVehicleIdsAndDateAndDeletedFalse(contractorId, vehicleIds, LocalDateTime.now(ZoneOffset.UTC));
        return list.parallelStream().map(shift -> {
            var vehicleShiftStatusResponse = new VehicleShiftStatusResponse();
            vehicleShiftStatusResponse.setVehicleId(shift.getVehicle().getId());
            vehicleShiftStatusResponse.setDriverId(shift.getDriver().getId());
            vehicleShiftStatusResponse.setOnline(shift.isActive());
            return vehicleShiftStatusResponse;
        }).toList();
    }

    private Iterable<VehicleDTO> findAll(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO,
                                         boolean withPagination) {
        var sort = getSort(searchDTO);
        if (withPagination) {
            var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
            return vehicleRepository.findAll(createSpec(contractorId, autoparkId, searchDTO), pageable)
                    .map(vehicle -> mapper.toDto(vehicle, null));
        } else {
            return vehicleRepository.findAll(createSpec(contractorId, autoparkId, searchDTO))
                    .stream()
                    .map(vehicle -> mapper.toDto(vehicle, null))
                    .toList();
        }
    }

    private Specification<Vehicle> createSpec(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO) {
        return (root, q, cb) -> {
            var autopark = root.join(Vehicle_.autopark);
            var contractor = autopark.join(Autopark_.contractor);

            var predicate = cb.equal(contractor.get(Contractor_.id), contractorId);
            if (autoparkId != null) {
                predicate = cb.and(predicate, cb.equal(autopark.get(Autopark_.id), autoparkId));
            }

            for (var entry : searchDTO.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case STATE_NUMBER ->
                                cb.and(predicate, cb.like(cb.lower(root.get(Vehicle_.stateNumber)), LIKE_FORMAT.formatted(value.toString().toLowerCase(Locale.ROOT))));
                        case AUTOPARK -> cb.and(predicate, cb.equal(autopark.get(Autopark_.id), value));
                        case MANUFACTURE_YEAR -> cb.and(predicate, cb.equal(root.get(Vehicle_.manufactureYear), value));
                        case MANUFACTURE_YEAR_START ->
                                cb.and(predicate, cb.lessThanOrEqualTo(root.get(Vehicle_.manufactureYear), (Integer) value));
                        case MANUFACTURE_YEAR_END ->
                                cb.and(predicate, cb.greaterThanOrEqualTo(root.get(Vehicle_.manufactureYear), (Integer) value));
                        case TRANSMISSION -> cb.and(predicate, cb.equal(root.get(Vehicle_.transmissionType), value));
                        case BRAND ->
                                cb.and(predicate, cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.brand)), LIKE_FORMAT.formatted(value.toString().toLowerCase(Locale.ROOT))));
                        case MODEL ->
                                cb.and(predicate, cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.name)), LIKE_FORMAT.formatted(value.toString().toLowerCase(Locale.ROOT))));
                        case VEHICLE_TYPE ->
                                cb.and(predicate, cb.equal(root.get(Vehicle_.vehicleType), VehicleType.valueOf(value.toString())));
                        case IN_EXPLOITATION ->
                                cb.and(predicate, cb.equal(root.get(Vehicle_.IN_EXPLOITATION), Boolean.valueOf(value.toString())));
                    };
                }
            }

            return predicate;
        };
    }

    private List<Vehicle> getFreeTransport(UUID contractorId, TransportSearchDTO searchDTO) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Vehicle> q = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = q.from(Vehicle.class);
        var autopark = root.join(Vehicle_.autopark);
        var contractor = autopark.join(Autopark_.contractor);

        var predicate = cb.equal(contractor.get(Contractor_.id), contractorId);
        predicate = cb.and(predicate, cb.equal(root.get(Vehicle_.IN_EXPLOITATION), true));

        if (searchDTO.getResult() != null && !searchDTO.getResult().isEmpty()) {
            List<Selection<?>> fields = new ArrayList<>();
            if (searchDTO.getResult().contains(ResponseFields.ID)) {
                fields.add(root.get(Vehicle_.id.getName()).alias("id"));
            } else {
                fields.add(cb.literal(null).alias("id"));
            }
            if (searchDTO.getResult().contains(ResponseFields.MODEL)) {
                fields.add(root.get(Vehicle_.model).get(CarModel_.name.getName()).alias("model"));
            } else {
                fields.add(cb.literal(null).alias("model"));
            }
            if (searchDTO.getResult().contains(ResponseFields.BRAND)) {
                fields.add(root.get(Vehicle_.model).get(CarModel_.brand.getName()).alias("brand"));
            } else {
                fields.add(cb.literal(null).alias("brand"));
            }
            if (searchDTO.getResult().contains(ResponseFields.STATE_NUMBER)) {
                fields.add(root.get(Vehicle_.stateNumber).alias("stateNumber"));
            } else {
                fields.add(cb.literal(null).alias("stateNumber"));
            }
            q.multiselect(fields).distinct(true);
        }
        var list = new ArrayList<Predicate>();
        if (searchDTO.getSearch() != null && !searchDTO.getSearch().isEmpty()) {
            list.add(predicate);
            Arrays.stream(searchDTO.getSearch().split(" ")).forEach(s -> {
                list.add(cb.or(
                        cb.like(cb.lower(root.get(Vehicle_.stateNumber)), cb.lower(cb.literal(LIKE_FORMAT.formatted(s)))),
                        cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.brand)), cb.lower(cb.literal(LIKE_FORMAT.formatted(s)))),
                        cb.like(cb.lower(root.get(Vehicle_.model).get(CarModel_.name)), cb.lower(cb.literal(LIKE_FORMAT.formatted(s))))
                ));
            });
            q.where(list.toArray(Predicate[]::new));
        } else {
            q.where(predicate);
        }
        q.orderBy(Direction.ASC.equals(searchDTO.getDirection()) ? cb.asc(getSortPath(searchDTO.getField(), root)) : cb.desc(getSortPath(searchDTO.getField(), root)));
        return entityManager.createQuery(q).getResultList();
    }

    private Sort getSort(VehicleSearchDTO searchDTO) {
        var sort = switch (searchDTO.getField()) {
            case MANUFACTURE_YEAR -> Sort.by(Vehicle_.MANUFACTURE_YEAR);
            case TRANSMISSION -> Sort.by(Vehicle_.TRANSMISSION_TYPE);
            case BRAND -> Sort.by(Vehicle_.MODEL + "." + CarModel_.BRAND);
            case MODEL -> Sort.by(Vehicle_.MODEL + "." + CarModel_.NAME);
            default -> Sort.by(Vehicle_.STATE_NUMBER);
        };
        return Direction.ASC.equals(searchDTO.getDirection()) ? sort.ascending() : sort.descending();
    }

    private Path<String> getSortPath(TransportSearchParameters field, Root<Vehicle> root) {
        return switch (field) {
            case BRAND -> root.get(Vehicle_.model).get(CarModel_.brand);
            case MODEL -> root.get(Vehicle_.model).get(CarModel_.name);
            default -> root.get(Vehicle_.stateNumber);
        };
    }

    private void decodeValues(TransportSearchDTO transportSearchDTO) {
        if (transportSearchDTO.getSearch() != null) {
            transportSearchDTO.setSearch(URLDecoder.decode(transportSearchDTO.getSearch(), StandardCharsets.UTF_8));
        }
    }
}
