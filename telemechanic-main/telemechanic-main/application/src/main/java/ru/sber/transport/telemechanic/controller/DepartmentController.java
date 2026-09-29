package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.dto.OrganizationWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with departments
 */
@RequestMapping("department")
@Validated
@Tag(name = "Подразделение", description = "Набор методов для работы с подразделениями")
public interface DepartmentController {
    
    /**
     * Get employees info by department id and personnel number
     *
     * @return (@ link EmployeeByDepartmentDto)
     */
    @PostMapping(value = "employees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка сотрудников", description = "Получение списка сотрудников в подразделении по табельному номеру")
    List<EmployeeInfoByDepartmentDto> searchEmployeesInfoByDepartment(@RequestBody SearchEmployeesInfoByDepartmentRequest request);
    
    /**
     * Get organization with internal autoparks.
     *
     * @return list {@link OrganizationWithAutoparkDto}.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение организаци", description = "Получение данных подраздлений с внутренними автопарками")
    OrganizationWithAutoparkDto getOrganizationWithIntertnalAutopark(@RequestParam UUID organizationId);
}
