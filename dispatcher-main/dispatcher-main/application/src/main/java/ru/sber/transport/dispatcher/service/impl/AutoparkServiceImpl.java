package ru.sber.transport.dispatcher.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Autopark_;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Contractor_;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;
import ru.sber.transport.dispatcher.dto.VehicleNormDto;
import ru.sber.transport.dispatcher.dto.search.AutoparkSearchDTO;
import ru.sber.transport.dispatcher.dto.search.AutoparkSearchParameters;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.messaging.senders.AutoparkSender;
import ru.sber.transport.dispatcher.service.AutoparkService;
import ru.sber.transport.dispatcher.service.ContractorService;
import ru.sber.transport.dispatcher.service.VerificationService;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static ru.sber.transport.dispatcher.exceptions.ConflictException.VEHICLE_COUNT_NORM_AVAILABLE_EXCEPTION_MESSAGE;

/**
 * Implementation of autopark controller service.
 */
@RequiredArgsConstructor
@Transactional
@Component
class AutoparkServiceImpl implements AutoparkService {

    private final ContractorService contractorService;

    private final AutoparkRepository autoparkRepository;

    private final VehicleRepository vehicleRepository;

    private final ShiftRepository shiftRepository;

    private final VerificationService verificationService;

    private final AutoparkSender autoparkSender;

    private static final String SQL_LIKE_FORMAT = "%%%s%%";

    @Override
    public AutoparkDTO add(UUID contractorId, NewAutoparkDTO newAutoparkDTO) {
        var exists = autoparkRepository.findByContractorIdAndNameAndActiveIsTrue(contractorId, newAutoparkDTO.getName());
        if (exists.isPresent()) {
            throw new DuplicateDataException(
                    Autopark.class, Map.of("id", exists.get().getId(), "name", newAutoparkDTO.getName())
            );
        }

        var contractor = contractorService.get(contractorId).orElseThrow(
                () -> new EntityNotFoundException(Contractor.class, contractorId));
        validateAutoparkVehicleCountNorm(contractor, newAutoparkDTO.getVehicleCountNorm(), null);

        var entity = Autopark.builder()
                .name(newAutoparkDTO.getName())
                .contractor(contractor)
                .vehicleCountNorm(newAutoparkDTO.getVehicleCountNorm())
                .routingId(newAutoparkDTO.getRoutingId()).build();

        entity = autoparkRepository.save(entity);
        autoparkSender.send(entity);

        return createResponse(entity);
    }

    @Override
    public void edit(UUID contractorId, UUID autoparkId, NewAutoparkDTO newAutoparkDTO) {
        var autoparkInDb = autoparkRepository.findById(autoparkId).orElseThrow(
                () -> new EntityNotFoundException(Autopark.class, autoparkId));
        var exists = autoparkRepository.existsByIdAndContractorIdAndActiveIsTrue(autoparkId, contractorId);
        verificationService.checkAutoParkToContractorRelation(newAutoparkDTO.getName(), contractorId, exists);
        validateAutoparkVehicleCountNormForUpdate(contractorId, autoparkInDb, newAutoparkDTO.getVehicleCountNorm());

        autoparkInDb = autoparkInDb.toBuilder()
                .id(autoparkId)
                .name(newAutoparkDTO.getName())
                .vehicleCountNorm(newAutoparkDTO.getVehicleCountNorm())
                .routingId(newAutoparkDTO.getRoutingId())
                .build();

        autoparkInDb = autoparkRepository.save(autoparkInDb);
        autoparkSender.send(autoparkInDb);
    }

    @Override
    public void delete(UUID contractorId, UUID autoparkId) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        var autopark = autoparkRepository.findById(autoparkId)
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, autoparkId));
        var vehicles = vehicleRepository.findAllByAutoparkId(autoparkId);
        var shifts = new ArrayList<Shift>();
        vehicles.forEach(vehicle -> shifts.addAll(shiftRepository.findAllByVehicleAndDeletedFalse(vehicle)));
        shifts.parallelStream().forEach(shift -> {
            verificationService.checkVehicleBusyness(shift);
            shift.setDeleted(true);
            vehicles.get(vehicles.indexOf(shift.getVehicle())).setActive(false);
        });
        autopark.setActive(false);
        autopark.setRoutingId(null);
        shiftRepository.saveAll(shifts);
        vehicleRepository.saveAll(vehicles);
        autopark = autoparkRepository.save(autopark);
        autoparkSender.send(autopark);
    }

    @Override
    public void deleteAllByContractorId(UUID contractorId) {
        var autoparks = autoparkRepository.findAllByContractorIdAndActiveIsTrue(contractorId);
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        autoparks.forEach(autopark -> {
            var vehicles = vehicleRepository.findAllByAutoparkId(autopark.getId());
            var shifts = new ArrayList<Shift>();
            vehicles.forEach(vehicle -> shifts.addAll(shiftRepository.findAllByVehicleAndDeletedFalse(vehicle)));
            shifts.forEach(shift -> {
                verificationService.checkVehicleBusyness(shift);
                shift.setDeleted(true);
                vehicles.get(vehicles.indexOf(shift.getVehicle())).setActive(false);
            });
            autopark.setActive(false);
            autopark.setRoutingId(null);
            shiftRepository.saveAll(shifts);
            vehicleRepository.saveAll(vehicles);
        });
        autoparkRepository.saveAll(autoparks);
        autoparkSender.sendAll(autoparks);
    }

    @Override
    public AutoparkDTO get(UUID contractorId, UUID autoparkId) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }

        return autoparkRepository.findByIdAndActiveTrue(autoparkId).map(this::createResponse)
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, autoparkId));
    }

    @Override
    public Page<AutoparkDTO> get(UUID contractorId, AutoparkSearchDTO searchDTO) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        var sort = Sort.sort(Autopark.class).by(Autopark::getName).ascending();
        var spec = getSpec(contractorId, searchDTO);
        var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        return autoparkRepository.findAll(spec, pageable).map(this::createResponse);
    }

    @Override
    public Collection<Autopark> getByContractorId(UUID contractorId) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        return autoparkRepository.findAllByContractorIdAndActiveIsTrue(contractorId);
    }

    @Override
    public AutoparkDTO getByName(String name) {
        return autoparkRepository.findByName(name)
                .map(this::createResponse)
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, name));
    }

    @Override
    public VehicleNormDto getVehicleNorm(@NonNull UUID contractorId) {
        var contractor = contractorService.get(contractorId).
                orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        var contractorVehicleNorm = contractor.getVehicleCountNorm();
        var totalCount = this.getByContractorId(contractorId)
                .stream()
                .mapToInt(autopark -> autopark.getVehicleCountNorm() != null ? autopark.getVehicleCountNorm() : 0)
                .sum();
        var availableCount = contractorVehicleNorm != null ? contractorVehicleNorm - totalCount : 0;
        return new VehicleNormDto(contractorVehicleNorm, availableCount, totalCount);
    }

    @Override
    public Autopark get(UUID id) {
        return id != null ? autoparkRepository.findByIdAndActiveTrue(id).orElse(null) : null;
    }


    /**
     * Create response by entity.
     *
     * @param entity source entity
     * @return response.
     */
    private AutoparkDTO createResponse(Autopark entity) {
        return new AutoparkDTO(
                entity.getId(),
                entity.getName(),
                null,
                entity.isActive(),
                entity.getRoutingId(),
                entity.getVehicleCountNorm());
    }

    private Specification<Autopark> getSpec(UUID contractorId, AutoparkSearchDTO searchDTO) {
        return (root, q, cb) -> {
            var predicate = cb.isNotNull(root.get(Autopark_.ID));
            predicate = cb.and(predicate, cb.equal(root.get(Autopark_.CONTRACTOR).get(Contractor_.ID), contractorId));
            for (var entry : searchDTO.getFilter().entrySet()) {
                if (entry.getKey() == AutoparkSearchParameters.NAME && entry.getValue() != null) {
                    predicate = cb.and(predicate, cb.like(root.get(Autopark_.NAME), SQL_LIKE_FORMAT.formatted(entry.getValue())));
                }
                if (entry.getKey() == AutoparkSearchParameters.ACTIVE && entry.getValue() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get(Autopark_.ACTIVE), Boolean.valueOf(String.valueOf(entry.getValue()))));
                }
                if (entry.getKey() == AutoparkSearchParameters.ROUTING_ID && entry.getValue() != null) {
                    predicate = cb.and(predicate, cb.equal(root.get(Autopark_.ROUTING_ID), searchDTO.getRoutingId()));
                }
            }
            q.distinct(true);
            return predicate;
        };
    }

    private void validateAutoparkVehicleCountNormForUpdate(UUID contractorId, Autopark autopark, Integer vehicleCountNorm) {
        if (vehicleCountNorm == null || autopark == null) return;
        VehicleNormDto vehicleNormDto = this.getVehicleNorm(contractorId);
        if (vehicleNormDto == null) return;
        int availableCount = autopark.getVehicleCountNorm() != null
                ? vehicleNormDto.getAvailableCount() + autopark.getVehicleCountNorm()
                : vehicleNormDto.getAvailableCount();
        if (vehicleCountNorm > availableCount) {
            throw new ConflictException(String.format(VEHICLE_COUNT_NORM_AVAILABLE_EXCEPTION_MESSAGE, vehicleCountNorm, availableCount));
        }
    }

    private void validateAutoparkVehicleCountNorm(Contractor contractor, Integer norma, Integer currentCount) {
        if (norma == null || (currentCount != null && norma <= currentCount)) return;

        if (contractor.getVehicleCountNorm() == null)
            throw new ConflictException("Необходимо задать нормативное количество автомобилей для внутреннего автопарка");

        int thresholdValue = autoparkRepository.findAllByContractorIdAndActiveIsTrue(contractor.getId())
                .stream()
                .mapToInt(Autopark::getVehicleCountNorm)
                .sum();

        if (norma > contractor.getVehicleCountNorm() - thresholdValue)
            throw new ConflictException("Нормативное количество автомобилей превышает допустимое значение");
    }

}
