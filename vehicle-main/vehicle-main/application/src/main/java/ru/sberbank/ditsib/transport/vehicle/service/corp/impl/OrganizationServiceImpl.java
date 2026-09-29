package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.vehicle.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapper;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implementation of service for working with organizations.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    private final OrganizationMapper organizationMapper;
    private final EmployeeService employeeService;
    
    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public List<Organization> getAllByIds(Set<UUID> ids) {
        return repository.findAllById(ids);
    }
    
    @Override
    public void delete(Organization entity) {
        entity.setActive(false);
        repository.save(entity);
    }
    
    @Override
    public Organization save(Organization entity) {
        return repository.save(entity);
    }
    
    @Override
    public List<OrganizationDto> getAll() {
        return repository.findAllByActiveIsTrueOrderByOfficialName().stream()
                         .map(organizationMapper::organizationToOrganizationDto)
                         .toList();
    }
    
    @Override
    public List<GetDepartmentsInfo> getAllWithDepartment(Set<UUID> ids) {
        var departments = repository.findByIdsWithActiveDepartments(ids);
        return departments.stream()
                         .collect(groupingBy(value -> Map.entry(value.organizationId(), value.organizationName())))
                         .entrySet()
                         .stream()
                         .map(entry -> new GetDepartmentsInfo(entry.getKey().getValue(),
                                                              entry.getKey().getKey(),
                                                              entry.getValue().stream()
                                                                   .map(organizationMapper::organizationWithDepartmentIntoDto)
                                                                   .toList()))
                         .toList();
    }
    
    @Override
    public OrganizationDto getByUserId(UUID userId) {
        var authenticatedEmployee = employeeService.getByUserId(userId);
        return organizationMapper.organizationToOrganizationDto(authenticatedEmployee.getOrganization());
    }
}
