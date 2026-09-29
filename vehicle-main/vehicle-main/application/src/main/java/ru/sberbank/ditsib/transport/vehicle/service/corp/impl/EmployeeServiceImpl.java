package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.vehicle.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;
import ru.sberbank.ditsib.transport.vehicle.exception.UserNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.mapper.EmployeeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of employee service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;

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
    public void saveOrUpdate(Employee entity) {
        var user = repository.findByUserId(entity.getUserId());
        if (user.isPresent()) {
            repository.save(user.get()
                                .setId(entity.getId())
                                .setHumanReadableId(entity.getHumanReadableId())
                                .setFirstName(entity.getFirstName())
                                .setLastName(entity.getLastName())
                                .setPatronymic(entity.getPatronymic())
                                .setMobilePhone(entity.getMobilePhone())
                                .setPersonnelNumber(entity.getPersonnelNumber())
                                .setUserId(entity.getUserId())
                                .setDepartment(entity.getDepartment())
                                .setPosition(entity.getPosition())
                                .setOrganization(entity.getOrganization()));
        } else {
            repository.save(entity);
        }
    }
    
    @Override
    public Employee getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public EmployeeDto getByPersonnelNumber(String personnelNumber, UUID userId) {
        var searcher = getByUserId(userId);
        return repository.findByPersonnelNumber(personnelNumber)
                .filter(foundPerson -> Objects.equals(searcher.getOrganization(), foundPerson.getOrganization()))
                .map(employeeMapper::employeeToEmployeeDto)
                .orElse(null);

    }
}
