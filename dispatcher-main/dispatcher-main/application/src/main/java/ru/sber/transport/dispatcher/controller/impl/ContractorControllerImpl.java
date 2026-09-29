package ru.sber.transport.dispatcher.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.dispatcher.controller.ContractorController;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.dispatcher.service.AuthCheckService;
import ru.sber.transport.dispatcher.service.ContractorControllerService;
import ru.sber.transport.dispatcher.service.VehicleService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера контрагентов.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class ContractorControllerImpl implements ContractorController {

    private static final String AUTOASSIGN_FIELD = "autoassign";

    private static final String MAIN_DISPATCHER_ID_FIELD = "mainDispatcherId";

    private static final String VEHICLE_COUNT_NORM_FIELD = "vehicleCountNorm";

    private final ContractorControllerService controllerService;

    private final VehicleService vehicleService;

    private final AuthCheckService authCheckService;

    @Override
    public ContractorDTO add(@Valid NewContractorDTO contractor) {
        return controllerService.add(contractor);
    }

    @Override
    public void edit(UUID id, @Valid NewContractorDTO contractor) {
        controllerService.edit(id, contractor);
    }

    @Override
    public void delete(UUID id) {
        controllerService.delete(id);
    }

    @Override
    public ContractorDTO get(UUID id) {
        return controllerService.get(id);
    }

    @Override
    public Page<VehicleDTO> getVehicles(UUID id, VehicleSearchDTO searchDTO) {
        return vehicleService.getAllPageable(id, null, searchDTO);
    }

    @Override
    public Page<ContractorDTO> getAll(ContractorSearchDTO contractorSearchDTO) {
        return controllerService.get(contractorSearchDTO);
    }

    @CheckOrganizationAccess
    @Override
    public void patchContractor(@Organization UUID contractorId, List<PatchData> data) {
        for (var patchData : data) {
            if (patchData.field().equals(AUTOASSIGN_FIELD)) {
                controllerService.editAutoassignFlag(contractorId, Boolean.valueOf(String.valueOf(patchData.value())));
            }
            if (patchData.field().equals(MAIN_DISPATCHER_ID_FIELD)) {
                controllerService.setMainDispatcher(contractorId, UUID.fromString(String.valueOf(patchData.value())));
            }
            if (patchData.field().equals(VEHICLE_COUNT_NORM_FIELD)) {
                controllerService.editVehicleCountNorm(contractorId, Integer.valueOf(String.valueOf(patchData.value())));
            }
        }
    }

    @Override
    public ru.sber.transport.dto.Page<TransportDTO> getAllFreeTransport(Integer version, Authentication authentication, TransportSearchDTO transportSearchDTO) {
        var contractorId = authCheckService.getContractorIdByToken((JwtAuthenticationToken) authentication);
        return controllerService.getAllFreeTransport(contractorId, transportSearchDTO);
    }

    @Override
    public Page<TransportDTO> getAllFreeTransportV2(Integer version, Authentication authentication, TransportSearchDTO transportSearchDTO) {
        var contractorId = authCheckService.getContractorIdByToken((JwtAuthenticationToken) authentication);
        return controllerService.getAllFreeTransportV2(contractorId, transportSearchDTO);
    }

    @Override
    public List<TransportDTO.Trip> getTransport(Authentication authentication, UUID vehicleId, TransportSearchDTO transportSearchDTO) {
        var contractorId = UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
        return controllerService.getTransport(contractorId, vehicleId, transportSearchDTO);
    }

    @Override
    public void linkContractor(LinkRequestDTO requestDto) {
        controllerService.linkContractor(requestDto);
    }

    @Override
    public VehicleNormDto getVehicleNorm(UUID contractorId) {
        return controllerService.getVehicleNorm(contractorId);
    }
}
