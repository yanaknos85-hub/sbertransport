package ru.sber.transport.dispatcher.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.mappers.ContractorMapper;
import ru.sber.transport.dispatcher.messaging.senders.ContractorSender;
import ru.sber.transport.dispatcher.service.*;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.sber.transport.dispatcher.exceptions.ConflictException.VEHICLE_COUNT_NORM_TOTAL_EXCEPTION_MESSAGE;

/**
 * Implementation of controller service for working with contractors.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractorControllerServiceImpl implements ContractorControllerService {

    private final ContractorService contractorService;

    private final ContractorSender contractorSender;

    private final ContractorMapper mapper;

    private final DispatcherService dispatcherService;

    private final VehicleService vehicleService;

    private final IntegrationClientService integrationClientService;

    private final AutoparkService autoparkService;

    private final DriverService driverService;

    @Override
    @Transactional
    public ContractorDTO add(NewContractorDTO contractor) {
        checkContractorExists(contractor.name(), contractor.tin(), null);
        var entity = new Contractor();
        mapper.update(entity, contractor);
        entity = contractorService.save(entity);
        var id = entity.getId();
        var dispatcher = Optional.ofNullable(contractor.mainDispatcher().id())
                .flatMap(dId -> dispatcherService.get(id, dId))
                .orElseGet(() -> dispatcherService.add(id, contractor.mainDispatcher()));
        entity.setMainDispatcher(dispatcher);
        entity = contractorService.save(entity);
        integrationClientService.add(entity.getId(), contractor.technicalAccountOwnerEmail(),
                contractor.technicalAccountLogin(), contractor.technicalAccountPassword());
        contractorSender.send(
                contractorService.findById(entity.getId()).orElseThrow(),
                IntegrationTypeDto.DISPATCHER_API);
        return createResponse(entity);
    }

    @Override
    @Transactional
    public void edit(UUID id, NewContractorDTO contractor) {
        var entity = contractorService.get(id).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
        checkContractorExists(contractor.name(), contractor.tin(), entity.getId());
        var newMainDispatcher = defineDispatcher(id, contractor.mainDispatcher().id(), contractor.mainDispatcher());
        entity.setMainDispatcher(newMainDispatcher);
        entity.setEmployeeCount(1);
        mapper.update(entity, contractor);
        var saved = contractorService.save(entity);
        contractorSender.send(saved, contractor.integrationType());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var entity = contractorService.get(id).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
        var deleted = contractorService.delete(entity);
        contractorSender.send(deleted, null);
        dispatcherService.get(id, null, Projection.SELECT).forEach(d -> dispatcherService.delete(id, d.id()));
        driverService.deleteAllByContractorId(id);
        autoparkService.deleteAllByContractorId(id);
    }

    @Override
    @Transactional
    public ContractorDTO get(UUID id) {
        return contractorService.get(id).map(this::createResponse).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
    }

    @SuppressWarnings("java:S3958")
    @Override
    @Transactional
    public Page<ContractorDTO> get(ContractorSearchDTO contractorSearchDTO) {
        var result = contractorService.getAll(contractorSearchDTO);
        return result.map(this::createResponse);
    }

    @SuppressWarnings("java:S3958")
    @Override
    @Transactional
    public Collection<ContractorDTO> getAll() {
        return contractorService.getAll().stream().map(this::createResponse).toList();
    }

    @Override
    @Transactional
    public void editAutoassignFlag(UUID contractorId, Boolean autoassign) {
        Contractor contractor = contractorService.get(contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));

        if (autoassign == null) {
            return;
        }
        contractor.setAutoassign(autoassign);
        var saved = contractorService.save(contractor);
        contractorSender.send(saved, null);
    }

    @Override
    @Transactional
    public void editVehicleCountNorm(UUID contractorId, Integer vehicleCountNorm) {
        Contractor contractor = contractorService.get(contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        VehicleNormDto vehicleNormDto = autoparkService.getVehicleNorm(contractorId);
        if (vehicleNormDto != null && vehicleCountNorm < vehicleNormDto.getTotalCount()) {
            throw new ConflictException(String.format(VEHICLE_COUNT_NORM_TOTAL_EXCEPTION_MESSAGE, vehicleCountNorm, vehicleNormDto.getTotalCount()));
        }
        contractor.setVehicleCountNorm(vehicleCountNorm);
        contractorService.save(contractor);
    }

    @Override
    @Transactional
    public void setMainDispatcher(UUID contractorId, UUID dispatcherId) {
        var dispatcher = dispatcherService.get(contractorId, dispatcherId).orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, dispatcherId));
        var contractor = contractorService.get(contractorId).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        contractor.setMainDispatcher(dispatcher);
        contractorService.save(contractor);
    }

    @Override
    public ru.sber.transport.dto.Page<TransportDTO> getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        return new ru.sber.transport.dto.Page<>(vehicleService.getAllByContractorAndFilters(contractorId, transportSearchDTO));
    }

    @Override
    public Page<TransportDTO> getAllFreeTransportV2(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        return vehicleService.getAllFreeTransport(contractorId, transportSearchDTO);
    }

    @Override
    public List<TransportDTO.Trip> getTransport(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO) {
        return vehicleService.getAllByContractorAndVehicleId(contractorId, vehicleId, transportSearchDTO);
    }

    @Override
    public void linkContractor(LinkRequestDTO requestDto) {
        var contractor = contractorService.findForLink(requestDto);
        integrationClientService.add(contractor.getId(), requestDto.contactPersonEmail(),
                requestDto.login(), requestDto.password());
    }

    @Override
    public VehicleNormDto getVehicleNorm(@NonNull UUID contractorId) {
        return autoparkService.getVehicleNorm(contractorId);
    }

    /**
     * Create response by entity.
     *
     * @param entity source entity.
     * @return response.
     */
    private ContractorDTO createResponse(Contractor entity) {
        return mapper.toDto(entity);
    }


    private void checkContractorExists(String name, String tin, UUID id) {
        var contractor = contractorService.isContractorExists(name, tin);
        if (contractor.isPresent() && !contractor.get().getId().equals(id)) {
            throw new DuplicateDataException(Contractor.class, Map.of("id", contractor.get().getId(),
                    "name", name,
                    "tin", tin
            ));
        }
    }

    private Dispatcher defineDispatcher(UUID contractorId, UUID mainDispatcherId, NewDispatcherDto mainDispatcher) {
        if (mainDispatcherId == null && mainDispatcher == null) {
            return null;
        }
        if (mainDispatcherId == null) {
            return dispatcherService.add(contractorId, mainDispatcher);
        } else {
            return dispatcherService.edit(contractorId, mainDispatcherId, mainDispatcher);
        }
    }
}
