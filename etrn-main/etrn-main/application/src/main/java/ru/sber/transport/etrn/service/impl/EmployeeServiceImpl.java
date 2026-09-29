package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.dao.EmployeeRepository;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.exceptions.UserNotFoundException;
import ru.sber.transport.etrn.service.EmployeeService;
import ru.sber.transport.etrn.util.ContextHelper;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;

    @Override
    public void save(Employee employee) {
        repository.save(employee);
    }

    @Override
    public void delete(Employee employee) {
        repository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Получить аутентифицированного сотрудника из Authentication.
     * Выбрасывает исключение, если сотрудник не найден в системе.
     *
     * @param authentication объект аутентификации.
     * @return Аутентифицированный сотрудник.
     */
    @Override
    @Transactional(readOnly = true)
    public Employee getAuthenticatedEmployee(Authentication authentication) {
        var userId = ContextHelper.getUserId(authentication);
        log.debug("Поиск сотрудника по userId={}", userId);

        return repository.findByUserIdWithDepartment(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получить сотрудника по ID {}, который не найден в системе", userId);
                    return new UserNotFoundException(userId);
                });
    }
}