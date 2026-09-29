package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.request.messaging.message.EmployeeMessage;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Реализация слушателя сотрудников.
 */
@RequiredArgsConstructor
public class EmployeeListenerImpl implements Consumer<Message<EmployeeMessage>> {
    
    private final EntityDTOMapper mapper;
    
    private final EmployeeService employeeService;
    
    private final DepartmentService departmentService;
    
    public void accept(Message<EmployeeMessage> message) {
        handleEmployees(message.getPayload().getId(), message.getPayload());
    }
    
    private void handleEmployees(UUID id, EmployeeMessage message) {
        var employeeId = Optional.ofNullable(id).orElse(message.getId());
        if (message.isDeleted()) {
            employeeService.get(employeeId).ifPresent(employeeService::delete);
        } else {
            Employee employee = employeeService.get(employeeId).orElse(null);
            if (employee == null) {
                employeeService.save(mapper.employeeMessageToEmployeeEntity(message));
            } else {
                Department department = departmentService.findOrCreateById(message.getDepartmentId());
                employeeService.save(employee.toBuilder()
                                             .department(department)
                                             .positionId(message.getPositionId())
                                             .supervisorId(message.getSupervisorId())
                                             .firstName(message.getFirstName())
                                             .lastName(message.getLastName())
                                             .patronymic(message.getPatronymic())
                                             .userId(message.getUserId())
                                             .mobilePhone(message.getMobilePhone())
                                             .personnelNumber(message.getPersonnelNumber())
                                             .costCenter(message.getCostCenter())
                                             .itinerantType(
                                                     ItinerantType.getByName(message.getItinerantType()).orElse(null))
                                             .marriageCertificateNumber(message.getMarriageCertificateNumber())
                                             .humanReadableId(message.getHumanReadableId()).build());
            }
        }
    }
    
}
