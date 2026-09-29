package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.reports.mappers.DepartmentMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.DepartmentListener;
import ru.sberbank.ditsib.transport.reports.service.DepartmentService;

@Component("departmentsInput")
@RequiredArgsConstructor
public class DepartmentListenerImpl implements DepartmentListener   {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper;

    @Override
    public void handleDepartments(DepartmentMessage message) {
        if (!message.isDeleted()) {
            departmentService.save(departmentMapper.fromMessage(message));
        }
    }
}
