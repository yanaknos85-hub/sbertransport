package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.DepartmentRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.exception.OrganizationHasNotDepartmentWithAutoparkException;
import ru.sber.transport.telemechanic.exception.OrganizationNotFoundException;
import ru.sber.transport.telemechanic.mapper.DepartmentMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.grpc.Departments;

import java.util.*;

/**
 * Implementation of department service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository repository;
    private final DepartmentMapper departmentMapper;
    private final Departments departments;
    private final OrganizationRepository organizationRepository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Department entity) {
        entity.setActive(false);
        repository.save(entity);
    }
    
    @Override
    public Department save(Department entity) {
        return repository.save(entity);
    }
    
    @Override
    public List<DepartmentDto> getByOrganizationId(UUID organizationId) {
        return departmentMapper.departmentListToDepartmentDtoList(repository.findAllByOrganizationIdOrderByDepartmentName(organizationId));
    }
    
    @Override
    public Set<UUID> getNotOrganizationIds(UUID organizationId, Set<UUID> departmentIdSet) {
        return repository.findNotOrganizationIds(organizationId, departmentIdSet);
    }
    
    @Override
    public List<TariffDepartmentResponse> getAllWithActiveTariff(UUID organizationId) {
        return repository.findAllByOrganizationIdWithActiveTariffWithChildren(organizationId);
    }
    
    @Override
    public void saveGrpcEntity(String message, UUID id) throws AwaitingSynchronizationException {
        var grpcEntity = Optional.ofNullable(departments.one(id)).orElseThrow(() -> {
            log.info(message);
            return new AwaitingSynchronizationException("Awaiting an department synchronization");
        });
        if (grpcEntity.getParentId() != null && this.get(grpcEntity.getParentId()).isEmpty()) {
            this.saveGrpcParentEntities(grpcEntity.getParentId());
        }
        save(grpcEntity);
    }
    
    @Override
    public void saveGrpcParentEntities(UUID parentId) throws AwaitingSynchronizationException {
        var departmentsToSave = new LinkedList<Department>();
        var searchRootDepartment = true;
        var searchId = parentId;
        while (searchRootDepartment) {
            var grpcEntity = departments.one(searchId);
            if (grpcEntity == null) {
                throw new AwaitingSynchronizationException("Awaiting a parent department synchronization, parentId:" + parentId);
            }
            departmentsToSave.addFirst(grpcEntity);
            searchId = grpcEntity.getParentId();
            if (searchId == null || this.get(searchId).isPresent()) {
                searchRootDepartment = false;
            }
        }
        departmentsToSave.forEach(this::save);
    }
    
    @Override
    public Optional<Department> getByAutoparkId(UUID id) {
        return repository.findByAutoparkId(id);
    }
    
    @Override
    public OrganizationWithAutoparkDto getAllWithInternalAutoPark(UUID organizationId) {
        var departmentList = repository.findOrganiztionWithAutopark(organizationId);
        
        if(departmentList.isEmpty()){
            throw new OrganizationHasNotDepartmentWithAutoparkException();
        }
        
        var organizationOptional = organizationRepository.findById(organizationId);
        if (organizationOptional.isEmpty()) {
            throw new OrganizationNotFoundException(organizationId);
        }
        
        return new OrganizationWithAutoparkDto(organizationOptional.get().getOfficialName(), departmentList);
    }
    
    @Override
    public List<UUID> getDepartmentIdsByAutoparkIds(List<UUID> autoparkIds) {
        if (autoparkIds != null && !autoparkIds.isEmpty()){
            return repository.getIdsByAutoparkIds(autoparkIds);
        }
        return Collections.emptyList();
    }
}
