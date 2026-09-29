package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.messaging.message.DepartmentTripRequestApproversMessage;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.mappers.DepartmentTripRequestApproversMapper;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import java.util.function.Consumer;

/**
 * Implementation of department approvers listener.
 */
@Slf4j
@RequiredArgsConstructor
public class DepartmentTripRequestApproversListenerImpl implements Consumer<Message<DepartmentTripRequestApproversMessage>> {
    
    private final DepartmentService departmentService;
    
    private final DepartmentTripRequestApproversMapper mapper;
    
    public void accept(Message<DepartmentTripRequestApproversMessage> message) {
        handleEmployees(message.getPayload());
    }
    
    private void handleEmployees(DepartmentTripRequestApproversMessage message) {
        Department department = departmentService.get(message.getDepartmentId()).orElseThrow(
                () -> new EntityNotFoundException(Department.class, message.getDepartmentId()));
        department.setApprovers(mapper.toEntities(message.getApprovers()));
        departmentService.save(department);
        
    }
}
