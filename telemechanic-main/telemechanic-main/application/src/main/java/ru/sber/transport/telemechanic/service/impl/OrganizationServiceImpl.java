package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithDepartmentDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.mapper.OrganizationMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.grpc.Organizations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of service for working with organization.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final OrganizationMapper organizationMapper;
    private final Organizations organizations;
    
    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Organization entity) {
        repository.save(entity.withActive(false));
    }
    
    @Override
    public Organization save(Organization entity) {
        return repository.save(entity);
    }
    
    @Override
    public List<OrganizationDto> getAll() {
        return repository.findAllActiveOrderByOfficialName();
    }
    
    @Override
    public List<OrganizationDto> getAllWithInternalContractor() {
        return repository.findAllActiveWithInternalContractorOrderByOfficialName();
    }
    
    @Override
    public List<OrganizationWithDepartmentDto> getAllWithDepartment(Set<UUID> uuids) {
        return repository.findAllByIdInOrderByOfficialName(uuids).stream()
                         .map(this::createOrganizationWithDepartmentDto)
                         .toList();
    }
    
    @Override
    public OrganizationDto getByUserId(UUID userId) {
        var authenticatedEmployee = employeeService.getByUserId(userId);
        return organizationMapper.organizationToOrganizationDto(authenticatedEmployee.getOrganization());
    }
    
    @Override
    public List<TariffDepartmentResponse> getAllDepartmentWithTariff(UUID id) {
        return departmentService.getAllWithActiveTariff(id);
    }
    
    @Override
    public String getFirstContactPhone(UUID id) {
        return repository.getFirstContactPhone(id);
    }
    
    @Override
    public void saveGrpcEntity(String message, UUID id) throws AwaitingSynchronizationException {
        this.save(Optional.ofNullable(organizations.one(id)).orElseThrow(() -> {
            log.info(message);
            return new AwaitingSynchronizationException("Awaiting an organization synchronization");
        }));
    }
    
    private OrganizationWithDepartmentDto createOrganizationWithDepartmentDto(Organization organization) {
        return new OrganizationWithDepartmentDto(organization.getOfficialName(), departmentService.getByOrganizationId(organization.getId()));
    }
}