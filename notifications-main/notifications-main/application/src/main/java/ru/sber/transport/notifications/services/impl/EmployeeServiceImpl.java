package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.NotificationContact;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.exception.UserNotFoundException;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationContactService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Реализация сервиса по работе с пользователями.
 */
@RequiredArgsConstructor
@Component
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final NotificationContactService notificationContactService;

    @Override
    public Optional<Employee> get(UUID employeeId) {
        return repository.findById(employeeId);
    }

    @Override
    public List<Employee> getByIds(List<UUID> employeeIds) {
        return repository.findAllById(employeeIds);
    }

    @Override
    public List<Employee> getPassengersByRequestIds(Set<UUID> requestIds) {
        return repository.findAllByRequestIds(requestIds);
    }

    @Override
    public Optional<Employee> getByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(repository::delete);
        notificationContactService.get(id)
                .map(NotificationContact::getId)
                .ifPresent(notificationContactService::deleteById);
    }

    @Override
    public void save(Employee employee) {
        repository.save(employee);
        notificationContactService.save(NotificationContact.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .phoneConfirmed(employee.isPhoneConfirmed())
                .build());
    }

    @Override
    public Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return repository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}

