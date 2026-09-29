package ru.sber.transport.contractor.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.controller.ContractorController;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.*;
import ru.sber.transport.contractor.dto.enums.ContractorProjection;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.dto.search.TransportSearchDTO;
import ru.sber.transport.contractor.service.ContractorControllerService;
import ru.sber.transport.contractor.service.EmployeeService;
import ru.sber.transport.contractor.service.InternalAutoParkService;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Реализация контроллера контрагентов.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class ContractorControllerImpl implements ContractorController {

    private static final String AUTOASSIGN_FIELD = "autoassign";

    private static final String CONTACT_PERSON_FIELD = "contactPerson";

    private static final String VEHICLE_COUNT_NORM_FIELD = "vehicleCountNorm";

    private final ContractorControllerService controllerService;

    private final EmployeeService employeeService;

    private final IntegrationConfig integrationConfig;

    private final InternalAutoParkService internalAutoParkService;

    @Override
    public ContractorDTO add(@Valid NewContractorDTO contractor, UUID organizationId, JwtAuthenticationToken token, HttpServletRequest request) {
        var orgId = getOrganizationId(organizationId, token);
        var authorization = request.getHeader("Authorization");
        return controllerService.add(contractor, orgId, authorization);
    }

    @Override
    public void edit(UUID id, @Valid NewContractorDTO contractor, UUID organizationId, JwtAuthenticationToken token, HttpServletRequest request) {
        var orgId = getOrganizationId(organizationId, token);
        var authorization = request.getHeader("Authorization");
        controllerService.edit(id, contractor, orgId, authorization);
    }

    @Override
    public void delete(UUID id, HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        controllerService.delete(id, authorization);
    }

    @Override
    public ContractorDTO get(UUID id) {
        return controllerService.get(id);
    }

    @Override
    public Iterable<ContractorDTO> getAll(Boolean paged, ContractorSearchDTO contractorSearchDTO, ContractorProjection projection, JwtAuthenticationToken token) {
        var jwt = token.getToken();
        var loggedInId = UUID.fromString(jwt.getId());
        var employee = employeeService.getByUserId(loggedInId).orElse(null);
        if (Boolean.TRUE.equals(jwt.getClaimAsBoolean("data_master"))) {
            return controllerService.get(Boolean.TRUE.equals(paged), contractorSearchDTO, Optional.ofNullable(projection).orElse(ContractorProjection.FULL), contractorSearchDTO.getOrganizationId());
        } else if (employee != null) {
            var organizationId = employee.getOrganizationId();
            if (contractorSearchDTO.getOrganizationId() != null && !organizationId.equals(contractorSearchDTO.getOrganizationId())) {
                throw new UnauthorizedException(employee.getId());
            }
            return controllerService.get(Boolean.TRUE.equals(paged), contractorSearchDTO, Optional.ofNullable(projection).orElse(ContractorProjection.FULL), organizationId);
        } else throw new UnauthorizedException(loggedInId);
    }

    @Override
    public void editAutoassignFlag(UUID contractorId, Boolean autoassign) {
        controllerService.editAutoassignFlag(contractorId, autoassign);
    }

    @Override
    public void patchContractor(UUID contractorId, List<PatchData> data, String token) {
        for (var patchData : data) {
            if (patchData.field().equals(AUTOASSIGN_FIELD)) {
                controllerService.editAutoassignFlag(contractorId, Boolean.valueOf(String.valueOf(patchData.value())));
            }
            if (patchData.field().equals(CONTACT_PERSON_FIELD)) {
                controllerService.editContactPerson(contractorId, patchData.value(), token);
            }
            if (patchData.field().equals(VEHICLE_COUNT_NORM_FIELD)) {
                controllerService.editVehicleCountNorm(contractorId, patchData, token);
            }
        }
    }

    @Override
    public Object getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        return controllerService.getAllFreeTransport(contractorId, transportSearchDTO);
    }

    @Override
    public Object getTransport(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO) {
        return controllerService.getTransport(contractorId, vehicleId, transportSearchDTO);
    }

    @Override
    public List<EnumRusNameDTO> getServiceTypes(boolean specialService) {
        return Arrays.stream(ServiceType.values()).filter(serviceType -> serviceType.isSpecialService() == specialService).map(s -> new EnumRusNameDTO(s.name(), s.getRusName())).toList();
    }

    @Override
    public List<EnumRusNameDTO> getContractorTypes(ServiceType serviceType) {
        return integrationConfig.getContractorTypes(serviceType);
    }

    @Override
    public VehicleNormDto getVehicleNorm(UUID organizationId, String token) {
        return internalAutoParkService.getVehicleNorm(organizationId, token);
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private <T> T getGet(Future<T> future) {
        return future.get();
    }

    private UUID getOrganizationId(UUID organizationId, JwtAuthenticationToken token) {
        var id = UUID.fromString(token.getToken().getId());
        var employee = employeeService.getByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, id));
        var orgId = employee.getOrganizationId();
        if (Boolean.TRUE.equals(token.getToken().getClaimAsBoolean("data_master"))) {
            orgId = Optional.ofNullable(organizationId).orElse(orgId);
        }
        return orgId;
    }
}
