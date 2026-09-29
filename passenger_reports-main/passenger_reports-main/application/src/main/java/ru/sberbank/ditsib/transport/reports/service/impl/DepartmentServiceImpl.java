package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.reports.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentDTO;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    
    private final RequestRepository repository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return departmentRepository.findById(id);
    }
    
    @Override
    public List<Department> get(List<UUID> ids) {
        return departmentRepository.findAllById(ids);
    }
    
    @Override
    public void delete(Department department) {
        departmentRepository.delete(department);
    }
    
    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }
    
    @Override
    public Department findOrCreateById(UUID id) {
        var department = departmentRepository.findById(id);
        return department.orElseGet(() -> departmentRepository.save(new Department(id)));
    }
    
    @Transactional
    @Override
    public void setDepartmentHierarchy(Map<UUID, Request> requestMap) {
        var departmentHierarchyMap = new HashMap<UUID, Map<Integer, Department>>();
        var requests = repository.findAllWithDepartments(requestMap.keySet())
                                 .stream().collect(Collectors.toMap(Request::getId, Function.identity()));
        
        requests.forEach((key, value) -> {
            if (requestMap.containsKey(key)) {
                var department = value.getPassenger().getDepartment();
                var departmentMap = departmentHierarchyMap.get(department.getId());
                if (departmentMap == null) {
                    departmentMap = getDepartmentHierarchy(department);
                    departmentHierarchyMap.put(department.getId(), departmentMap);
                }
            }
        });
    }
    
    @Override
    public Map<Integer, Department> getDepartmentHierarchy(Department department) {
        return getDepartmentHierarchy(department.getId());
    }
    
    @Override
    public Map<Integer, Department> getDepartmentHierarchy(UUID departmentId) {
        var curDepartment = departmentRepository.getById(departmentId);
        
        var departmentList = new LinkedList<Department>();
        departmentList.addFirst(curDepartment);
        while (curDepartment.getParentId() != null) {
            curDepartment = departmentRepository.getById(curDepartment.getParentId());
            departmentList.addFirst(curDepartment);
        }
        
        var hierarchyMap = new HashMap<Integer, Department>();
        for (var i = 0; !departmentList.isEmpty(); i++) {
            hierarchyMap.put(i + 1, departmentList.poll());
        }
        
        return hierarchyMap;
    }
    
    @Override
    public Set<UUID> getDepartmentWithAllChildren(HashSet<UUID> departmentIds) {
        var foundedDepartments = new CopyOnWriteArraySet<UUID>();
        departmentIds.forEach(departmentId -> {
            foundedDepartments.add(departmentId);
            var children = departmentRepository.findAllByParentId(departmentId);
            log.debug("Найдено " + children.size() + " дочерних подразделений для подразделения " + departmentId.toString());
            var childrenForCycle = new CopyOnWriteArrayList<>(children);
            childrenForCycle.parallelStream().forEach(child -> foundedDepartments.add(child.getId()));
        });
        log.debug("Найдено " + foundedDepartments.size() + " подразделений");
        return foundedDepartments;
    }
    
    @Override
    public List<Department> findDepartments(UUID organizationId, DepartmentDTO departmentDTO) {
        
        List<Department> firstLevel;
        List<Department> secondLevel;
        List<Department> thirdLevel;
        List<Department> fourthLevel;
        List<Department> fifthLevel;
        var neededLevel = departmentDTO.getDepartmentLevel();
    
        if (neededLevel == 1) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIsNull(organizationId);
        }
        
        if (departmentDTO.getDepartment1() != null && !departmentDTO.getDepartment1().isEmpty()) {
            firstLevel = departmentRepository.findAllByOrganizationIdAndDepartmentNameIn(organizationId, departmentDTO.getDepartment1());
        } else {
            firstLevel = departmentRepository.findAllByOrganizationIdAndParentIdIsNull(organizationId);
        }
    
        List<UUID> firstIds = firstLevel.stream().map(Department::getId).distinct().toList();
        
        if (neededLevel == 2) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, firstIds);
        }
    
        if (departmentDTO.getDepartment2() != null && !departmentDTO.getDepartment2().isEmpty()) {
            secondLevel = departmentRepository.findAllByOrganizationIdAndDepartmentNameInAndParentIdIn(
                    organizationId,
                    departmentDTO.getDepartment2(),
                    firstIds);
        } else {
            secondLevel = departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, firstIds);
        }
    
        List<UUID> secondIds = secondLevel.stream().map(Department::getId).distinct().toList();
        
        if (neededLevel == 3) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, secondIds);
        }
        
        if (departmentDTO.getDepartment3() != null && !departmentDTO.getDepartment3().isEmpty()) {
            thirdLevel = departmentRepository.findAllByOrganizationIdAndDepartmentNameInAndParentIdIn(
                    organizationId,
                    departmentDTO.getDepartment3(),
                    secondIds);
        } else {
            thirdLevel = departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, secondIds);
        }
    
        if (neededLevel == 4) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId,
                                                                             thirdLevel.stream().map(Department::getId).distinct().toList());
        }
    
        List<UUID> thirdIds = thirdLevel.stream().map(Department::getId).distinct().toList();
        if (departmentDTO.getDepartment4() != null && !departmentDTO.getDepartment4().isEmpty()) {
            fourthLevel = departmentRepository.findAllByOrganizationIdAndDepartmentNameInAndParentIdIn(
                    organizationId,
                    departmentDTO.getDepartment4(),
                    thirdIds);
        } else {
            fourthLevel = departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, thirdIds);
        }
        
        if (neededLevel == 5) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId,
                                                                             fourthLevel.stream().map(Department::getId).distinct().toList());
        }
    
        List<UUID> fourthIds = fourthLevel.stream().map(Department::getId).distinct().toList();
        if (departmentDTO.getDepartment5() != null && !departmentDTO.getDepartment5().isEmpty()) {
            fifthLevel = departmentRepository.findAllByOrganizationIdAndDepartmentNameInAndParentIdIn(
                    organizationId,
                    departmentDTO.getDepartment5(),
                    fourthIds);
        } else {
            fifthLevel = departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId, fourthIds);
        }
    
        if (neededLevel == 6) {
            return departmentRepository.findAllByOrganizationIdAndParentIdIn(organizationId,
                                                                             fifthLevel.stream().map(Department::getId).distinct().toList());
        }
    
        return Collections.emptyList();
    }
}
