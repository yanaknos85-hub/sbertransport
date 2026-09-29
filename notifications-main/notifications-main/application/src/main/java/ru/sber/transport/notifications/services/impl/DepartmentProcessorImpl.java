package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.*;

import java.util.List;

@Slf4j
@EnableAsync
@RequiredArgsConstructor
@Component
class DepartmentProcessorImpl implements Processor<Department> {
    
    private final DepartmentService departmentService;
    
    private final NotificationService notificationService;
    
    private final NotificationSettingsService settingsService;
    
    private final EmployeeService employeeService;
    
    private final List<NotificationProcessor> notificationProcessors;

    private final ObjectMapper objectMapper;
    
    @Override
    public void process(Department department) throws JsonProcessingException {
        if (department.getDepartmentHeadId() == null) {
            log.warn("DepartmentProcessorImpl: process: DepartmentHeadId is null for department " + department.getId());
            return;
        }

        department = departmentService.get(department.getId()).orElseThrow();
        var notificationClass = NotificationClass.USER_ASSIGNMENT;
        var notificationType = NotificationType.ASSIGNMENT;
        var notificationSettings = settingsService.get(department.getOrganizationId(),
                notificationClass,
                notificationType);
    
        var departmentNotification = new Notification();
        departmentNotification.setReceiverId(department.getDepartmentHeadId());
        departmentNotification.setSent(true);
        departmentNotification.setSettings(notificationSettings);
        departmentNotification.setEntity(objectMapper.writeValueAsString(department));
        var savedDepartment = notificationService.save(departmentNotification);
        notificationProcessors.forEach(processor -> processor.process(savedDepartment));
    }
}
