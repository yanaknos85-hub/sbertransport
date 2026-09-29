package ru.sberbank.ditsib.transport.request.controller.carsharing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.carsharing.CarsharingJoinRequestController;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingJoinAndCalculatedDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.NewCarsharingJoinRequestDTO;
import ru.sberbank.ditsib.transport.request.service.CarsharingJoinRequestService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@E2EController
public class CarsharingJoinRequestControllerImpl implements CarsharingJoinRequestController {
    
    private final CarsharingJoinRequestService joinRequestService;
    
    private final OrganizationService organizationService;
    
    private final EmployeeService employeeService;
    
    @CheckOrganizationAccess
    @Override
    public List<CarsharingJoinAndCalculatedDTO> getCarsharingJoinsForEmployee(
            List<CalculatedDto> calculatedList,
            @ru.sber.transport.authorization.annotations.Organization UUID organizationId,
            UUID employeeId
                                                                             ) {
        organizationService.check(organizationId);
        employeeService.check(employeeId);
        return joinRequestService.getCarsharingJoinsForEmployee(calculatedList, organizationId, employeeId);
    }
    
    @CheckOrganizationAccess
    @Override
    public GetCarsharingJoinRequestDTO getCreatedOrBlank(
            @ru.sber.transport.authorization.annotations.Organization UUID organizationId, UUID employeeId
                                                        ) {
        organizationService.check(organizationId);
        Employee employee = employeeService.getEmployee(employeeId);
        return joinRequestService.getCreatedOrBlank(organizationId, employee);
    }
    
    @CheckOrganizationAccess
    @Override
    public GetCarsharingJoinRequestDTO create(
            NewCarsharingJoinRequestDTO joinRequestDto,
            @ru.sber.transport.authorization.annotations.Organization UUID organizationId, UUID employeeId
                                             ) {
        Organization organization = organizationService.getOrganization(organizationId);
        Employee employee = employeeService.getEmployee(employeeId);
        return joinRequestService.create(joinRequestDto, organization, employee);
    }
    
    @Override
    public void update(NewCarsharingJoinRequestDTO joinRequestDto, UUID joinRequestId) {
        CarsharingJoinRequest joinRequestFromDb = joinRequestService.getById(joinRequestId);
        joinRequestService.update(joinRequestDto, joinRequestFromDb);
    }
    
    @Override
    public void delete(UUID joinRequestId) {
        CarsharingJoinRequest joinRequestFromDb = joinRequestService.getById(joinRequestId);
        joinRequestService.delete(joinRequestFromDb);
    }
}
