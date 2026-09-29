package ru.sberbank.ditsib.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

/**
 * Адаптер для работы с сервисом сотрудников.
 * Реализует взаимодействие с внешним gRPC сервисом корпоративных данных.
 * Предоставляет методы для получения информации о сотрудниках.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee getEmployeeByPersonnelNumber(String personnelNumber) {
        return employeeRepository.findByPersonnelNumber(personnelNumber).orElseThrow(() ->
                new EntityNotFoundException("Сотрудник с табельным номером: " + personnelNumber + " не найден."));
    }

    @Override
    public Optional<Employee> get(UUID id) {
        return employeeRepository.findById(id);
    }

    @Override
    public void delete(Employee entity) {
        entity.setActive(false);
        employeeRepository.save(entity);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }
} 