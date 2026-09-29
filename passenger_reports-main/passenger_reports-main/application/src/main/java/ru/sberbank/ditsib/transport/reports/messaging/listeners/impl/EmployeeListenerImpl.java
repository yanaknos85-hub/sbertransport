package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.EmployeeListener;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;
import ru.sberbank.ditsib.transport.reports.service.PositionService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация слушателя сотрудников.
 */
@RequiredArgsConstructor
@Component("employeesInput")
public class EmployeeListenerImpl implements EmployeeListener   {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final OrganizationService organizationService;

    @Override
    @Transactional
    public void handleEmployees(UUID id, EmployeeMessage message) {
        var employeeId = Optional.ofNullable(id).orElse(message.getId());
        if (!message.isDeleted()) {
            var employee = new Employee();
            if (message.getDepartmentId() != null) {
                var department = departmentService.findOrCreateById(message.getDepartmentId());
                employee.setDepartment(department);
            }
            if (message.getPositionId() != null) {
                var position = positionService.findOrCreatePositionById(message.getPositionId());
                employee.setPosition(position);
            }
            if (message.getOrganizationId() != null) {
                var organization = organizationService.findOrCreateById(message.getOrganizationId());
                employee.setOrganization(organization);
            }
            employee.setId(employeeId);
            employee.setUserId(message.getUserId());
            employee.setPersonnelNumber(message.getPersonnelNumber());
            employee.setFirstName(message.getFirstName());
            employee.setLastName(message.getLastName());
            employee.setPatronymic(message.getPatronymic());
            employee.setHumanReadableId(message.getHumanReadableId());
            employee.setMobilePhone(message.getMobilePhone());
            if (message.getItinerantType() != null) {
                employee.setItinerantType(ItinerantType.valueOf(message.getItinerantType()));
            }
            employee.setCostCenter(message.getCostCenter());
            employee.setMarriageCertificateNumber(message.getMarriageCertificateNumber());
            employeeService.save(employee);
        }
    }
}
