package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.EmployeeDTO;

import java.util.UUID;

@Mapper
public interface EmployeeMapper {
    
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "mvz", source = "costCenter")
    @Mapping(target = "organizationId", source = "department.organization.id")
    @Mapping(target = "organizationName", source = "department.organization.officialName")
    EmployeeDTO toDto(Employee employee);
    
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "department.departmentName", source = "departmentName")
    @Mapping(target = "costCenter", source = "mvz")
    @Mapping(target = "department.organization.id", source = "organizationId")
    @Mapping(target = "department.organization.officialName", source = "organizationName")
    Employee toModel(EmployeeDTO employee);
    
    void updateOrganization(@MappingTarget EmployeeDTO author, UUID organizationId);
}
