package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.DepartmentController;
import ru.sber.transport.telemechanic.dto.OrganizationWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.List;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class DepartmentControllerImpl implements DepartmentController {
    
    private final EmployeeService employeeService;
    
    private final DepartmentService departmentService;
    
    @Override
    public List<EmployeeInfoByDepartmentDto> searchEmployeesInfoByDepartment(SearchEmployeesInfoByDepartmentRequest request) {
        return employeeService.searchEmployeeInfoByDepartment(request);
    }
    
    @Override
    public OrganizationWithAutoparkDto getOrganizationWithIntertnalAutopark(UUID organizationId) {
        return departmentService.getAllWithInternalAutoPark(organizationId);
    }
}
