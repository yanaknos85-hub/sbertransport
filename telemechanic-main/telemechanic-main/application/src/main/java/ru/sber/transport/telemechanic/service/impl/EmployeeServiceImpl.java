package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.EmployeeRepository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;
import ru.sber.transport.telemechanic.exception.DepartmentNotActiveException;
import ru.sber.transport.telemechanic.exception.DepartmentNotFoundException;
import ru.sber.transport.telemechanic.exception.UserNotFoundException;
import ru.sber.transport.telemechanic.mapper.EmployeeMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of employee service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final DepartmentService departmentService;
    private final EmployeeRepository repository;
    
    private final EmployeeMapper mapper;
    
    @Override
    public Optional<Employee> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Employee entity) {
        entity.setActive(false);
        repository.save(entity);
    }
    
    @Override
    public Employee save(Employee entity) {
        return repository.save(entity);
    }
    
    @Override
    public Employee getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    @Override
    public List<GetMedicDto> getMedicByFIO(String fio) {
        return repository.findEmployeesByFullNameIndexContainingIgnoreCase(fio.toLowerCase().replaceAll("\\s", ""))
                         .stream()
                         .map(mapper::employeeToGetMedicDto)
                         .toList();
    }
    
    @Override
    public List<EmployeeInfoByDepartmentDto> searchEmployeeInfoByDepartment(SearchEmployeesInfoByDepartmentRequest request) {
        var department = departmentService.get(request.departmentId())
                                          .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));
        if (!department.isActive()) {
            throw new DepartmentNotActiveException(request.departmentId());
        }
        
        return repository.searchByDepartmentAndPersonnelNumber(department.getId(), request.personnelNumber());
    }

    @Override
    public Long getDigitIdByUserId(UUID userId) {
        return repository.findDigitIdByUserId(userId);
    }
}