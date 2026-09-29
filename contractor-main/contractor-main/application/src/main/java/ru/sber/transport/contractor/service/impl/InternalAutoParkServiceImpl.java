package ru.sber.transport.contractor.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.VehicleNormDto;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;
import ru.sber.transport.contractor.dto.internal.*;
import ru.sber.transport.contractor.exceptions.ClientFeignException;
import ru.sber.transport.contractor.exceptions.ConflictException;
import ru.sber.transport.contractor.feign.BranchesClient;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.feign.StaffClient;
import ru.sber.transport.contractor.messaging.senders.EmployeeRoleSender;
import ru.sber.transport.contractor.service.ContractorService;
import ru.sber.transport.contractor.service.EmployeeService;
import ru.sber.transport.contractor.service.InternalAutoParkService;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class InternalAutoParkServiceImpl implements InternalAutoParkService {

    private static final String ORGANIZATION_ID = "organizationId";
    private static final String BRANCH_ID = "branchId";
    private static final String AUTOPARK_ID = "autoparkId";
    private static final String SPECIALITY = "speciality";

    private final EmployeeService employeeService;

    private final ContractorService contractorService;

    private final IntegrationConfig config;

    private final StaffClient staffClient;

    private final BranchesClient branchesClient;

    private final EmployeeRoleSender employeeRoleSender;

    private final InternalClient internalClient;

    @Value("${internal.autopark.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String adminRole;

    @Value("${internal.autopark.main-dispatcher.role:ROLE_MAIN_DISPATCHER_CONTRACTOR}")
    private String mainDispatcherRole;

    @Value("${internal.autopark.dispatcher.role:ROLE_DISPATCHER_CONTRACTOR}")
    private String dispatcherRole;

    @Value("${internal.autopark.driver.role:ROLE_DRIVER_CONTRACTOR}")
    private String driverRole;

    @Override
    public void createStaff(JwtAuthenticationToken token, CreateStaffDto createStaffDTO, String authorization) {
        var user = getUser();
        var organizationId = getOrganizationId(createStaffDTO.organizationId(), user.getOrganizationId(), token);
        var employee = employeeService.getByUserId(createStaffDTO.employeeId()).orElseThrow(() -> new EntityNotFoundException(Employee.class, createStaffDTO.employeeId()));
        if(!Objects.equals(employee.getOrganizationId(), organizationId)){
            throw new ConflictException("Сотрудник не принадлежит организации внутреннего автопарка");
        }
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        switch (createStaffDTO.speciality()) {
            case MANAGER, DISPATCHER -> {
                var dispatcherStaff = new DispatcherStaffDto(createStaffDTO.lastName(), createStaffDTO.firstName(),
                        createStaffDTO.patronymic(), createStaffDTO.phone(), createStaffDTO.email(),
                        createStaffDTO.employeeId(), createStaffDTO.branchId(), createStaffDTO.ewbCreationPossibility(),
                        createStaffDTO.personnelNumber(), createStaffDTO.attorneyNumber(), createStaffDTO.issueDate(),
                        createStaffDTO.expiryDate(), createStaffDTO.creationSystem());
                var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
                try {
                    staffClient.addDispatcher(baseUrl, contractor.getExternalId(), dispatcherStaff, authorization);
                } catch (FeignException.FeignClientException e) {
                    throw new ClientFeignException("Произошел конфликт данных во время создания диспетчера во внешнем сервисе.", e);
                }
                if (createStaffDTO.speciality().equals(StaffSpeciality.MANAGER)){
                    employeeRoleSender.send(createStaffDTO.employeeId(), mainDispatcherRole);
                } else {
                    employeeRoleSender.send(createStaffDTO.employeeId(), dispatcherRole);
                }
            }
            case PASSENGER_DRIVER, CARGO_DRIVER -> {
                var driverStaff = new DriverStaffDto(
                        createStaffDTO.lastName(), createStaffDTO.firstName(),
                        createStaffDTO.patronymic(), createStaffDTO.phone(),
                        createStaffDTO.email(), createStaffDTO.employeeId(),
                        createStaffDTO.speciality().equals(StaffSpeciality.PASSENGER_DRIVER) ?
                                StaffSpeciality.DriverStaffSpeciality.PASSENGER : StaffSpeciality.DriverStaffSpeciality.CARGO,
                        true, createStaffDTO.branchId(), createStaffDTO.personnelNumber(), createStaffDTO.snils(),
                        createStaffDTO.tin(), createStaffDTO.issueDate(), createStaffDTO.expiryDate(),
                        createStaffDTO.driverLicenseNumber(), createStaffDTO.driverLicenses()
                );
                var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
                try {
                    staffClient.addDriver(baseUrl, contractor.getExternalId(), driverStaff, authorization);
                }  catch (FeignException.FeignClientException e) {
                    throw new ClientFeignException("Произошел конфликт данных во время создания водителя во внешнем сервисе.", e);
                }
                employeeRoleSender.send(createStaffDTO.employeeId(), driverRole);
            }
        }
    }

    @Override
    public Page<StaffDto> getStaff(JwtAuthenticationToken token, GetStaffDto getStaffDto, String authorization) {
        var user = getUser();
        var organizationId = getOrganizationId(getStaffDto.getOrganizationId(), user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        return switch (getStaffDto.getSpeciality()) {
            case DISPATCHER ->
                    staffClient.getDispatchers(baseUrl, contractor.getExternalId(), getStaffDto.getPage(), getStaffDto.getSize(), true, authorization);
            case DRIVER ->
                    staffClient.getDrivers(baseUrl, contractor.getExternalId(), getStaffDto.getPage(), getStaffDto.getSize(), true, authorization);
        };
    }

    @Override
    public void deleteStaff(JwtAuthenticationToken token, GetStaffDto.Speciality speciality, UUID orgId, UUID externalId, String authorizationHeader) {
        var user = getUser();
        var organizationId = getOrganizationId(orgId, user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        switch (speciality) {
            case DISPATCHER ->
                    staffClient.deleteDispatcher(baseUrl, contractor.getExternalId(), externalId, authorizationHeader);
            case DRIVER ->
                    staffClient.deleteDriver(baseUrl, contractor.getExternalId(), externalId, authorizationHeader);
        }
    }

    @Override
    public void createBranch(JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorization) {
        var user = getUser();
        var organizationId = getOrganizationId(createBranchDto.organizationId(), user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        branchesClient.addBranch(baseUrl, contractor.getExternalId(), new BranchDto(createBranchDto.name(), createBranchDto.departmentId(), createBranchDto.vehicleCountNorm()), authorization);
    }

    @Override
    public void updateBranch(UUID externalId, JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorization) {
        var user = getUser();
        var organizationId = getOrganizationId(createBranchDto.organizationId(), user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        try {
            branchesClient.updateBranch(baseUrl, contractor.getExternalId(), externalId, new BranchDto(createBranchDto.name(), createBranchDto.departmentId(), createBranchDto.vehicleCountNorm()), authorization);
        } catch (FeignException.FeignClientException e) {
            throw new ClientFeignException("Произошла ошибка во время обновления филиала во внешнем сервисе.", e);
        }
    }

    @Override
    public void deleteBranch(UUID externalId, JwtAuthenticationToken token, UUID organizationId, String authorization) {
        var user = getUser();
        var finalOrganizationId = getOrganizationId(organizationId, user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(finalOrganizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        branchesClient.deleteBranch(baseUrl, contractor.getExternalId(), externalId, authorization);
    }

    @Override
    public Page<BranchResponseDto> getBranches(JwtAuthenticationToken token, GetBranchDto getBranchDto, String authorization) {
        var user = getUser();
        var organizationId = getOrganizationId(getBranchDto.getOrganizationId(), user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        return branchesClient.getBranches(baseUrl, contractor.getExternalId(), getBranchDto.getName(),
                getBranchDto.getPage(), getBranchDto.getSize(), true, getBranchDto.getDepartmentId(), authorization);
    }

    @Override
    public VehicleNormDto getVehicleNorm(UUID organizationId, String authorization) {
        var contractor = contractorService.getInternalAutoPark(organizationId, null)
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        return internalClient.getVehicleNorm(baseUrl, contractor.getExternalId(), authorization);
    }

    @Override
    public void patchStaff(JwtAuthenticationToken token, UUID externalId, List<PatchData> data, StaffSpeciality speciality, String authorization) {
        Map<String, Serializable> patchDataMap = data.stream().filter(pdv -> pdv.value() != null).collect(Collectors.toMap(PatchData::field, PatchData::value));
        var user = getUser();
        var organizationId = getOrganizationId(UUID.fromString(String.valueOf(patchDataMap.get(ORGANIZATION_ID))), user.getOrganizationId(), token);
        var contractor = contractorService.getInternalAutoPark(organizationId, null).orElseThrow(() -> new EntityNotFoundException(Contractor.class, organizationId));
        var baseUrl = URI.create(config.getClientUrl(contractor.getContractorType()));
        data.removeIf(pdv -> pdv.field().equals(ORGANIZATION_ID));
        data.replaceAll(pdv -> pdv.field().equals(BRANCH_ID) ? new PatchData(AUTOPARK_ID, pdv.value()) : pdv);
        try {
            switch (speciality) {
                case DISPATCHER ->
                        staffClient.patchDispatcher(baseUrl, contractor.getExternalId(), externalId, data, authorization);
                case PASSENGER_DRIVER, CARGO_DRIVER -> {
                    data.add(new PatchData(SPECIALITY, speciality.equals(StaffSpeciality.PASSENGER_DRIVER)
                            ? StaffSpeciality.DriverStaffSpeciality.PASSENGER
                            : StaffSpeciality.DriverStaffSpeciality.CARGO));
                    staffClient.patchDriver(baseUrl, contractor.getExternalId(), externalId, data, authorization);
                }
            }
        } catch (FeignException.FeignClientException e) {
            throw new ClientFeignException("Произошла ошибка во время обновления сотрудника во внешнем сервисе.", e);
        }
    }

    private Employee getUser(){
        var userId = ControllerUtils.currentUser();
        return employeeService.getByUserId(userId).orElseThrow(() -> new EntityNotFoundException(Employee.class, userId));
    }

    @SuppressWarnings("unchecked")
    private UUID getOrganizationId(UUID currentOrganizationId, UUID userOrganizationId, JwtAuthenticationToken token){
        return ((Collection<? extends String>) token.getToken().getClaims().get("roles")).contains(adminRole)
                && ControllerUtils.isDataMaster() ? currentOrganizationId : userOrganizationId;
    }
}
