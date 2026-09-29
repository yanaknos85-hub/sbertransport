package ru.sberbank.ditsib.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.database.model.Position;
import ru.sberbank.ditsib.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.mappers.EmployeeMapper;
import ru.sberbank.ditsib.provider.EmployeeProvider;
import ru.sberbank.ditsib.service.DepartmentService;
import ru.sberbank.ditsib.service.EmployeeService;
import ru.sberbank.ditsib.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Objects;

/**
 * Реализация провайдера сотрудников.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class EmployeeProviderImpl implements EmployeeProvider {

    private final EmployeeService service;
    private final DepartmentService departmentService;
    private final PositionService positionService;

    private final EmployeeMapper employeeMapper;
    private static final String ERROR_MESSAGE = "Can't save employee id:%s, personnelNumber:%s, positionId:%s, " +
            "organizationId:%s, departmentId:%s, ";

    @Override
    public void delete(EmployeeMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }

    @Override
    public void save(EmployeeMessage message) {
        if (Objects.isNull(message.getDepartmentId())) {
            var errorMessage = String.format(ERROR_MESSAGE + "department isn't present",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            log.info(errorMessage);
        } else if (departmentService.get(message.getDepartmentId()).isEmpty()) {
            var errorMessage = String.format(ERROR_MESSAGE + "awaiting department synchronization",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else if (Objects.isNull(message.getPositionId())) {
            var errorMessage = String.format(ERROR_MESSAGE + "position isn't present",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            log.info(errorMessage);
        } else if (positionService.get(message.getPositionId()).isEmpty()) {
            var errorMessage = String.format(ERROR_MESSAGE + "awaiting position synchronization",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else {
            var dbEntityOptional = service.get(message.getId());
            if (dbEntityOptional.isPresent()) {
                service.save(dbEntityOptional.get()
                        .withActive(true)
                        .withDepartment(departmentService.get(message.getDepartmentId()).orElse(
                                Department.builder()
                                        .id(message.getDepartmentId())
                                        .build()
                        ))
                        .withPosition(positionService.get(message.getPositionId()).orElse(
                                Position.builder()
                                        .id(message.getPositionId())
                                        .build()
                        ))
                        .withFirstName(message.getFirstName())
                        .withLastName(message.getLastName())
                        .withPatronymic(message.getPatronymic())
                        .withHumanReadableId(message.getHumanReadableId())
                        .withPersonnelNumber(message.getPersonnelNumber())
                        .withMobilePhone(message.getMobilePhone())
                        .withUserId(message.getUserId()));
            } else {
                service.save(employeeMapper.employeeMessageToEmployee(message));
            }
        }
    }
}
