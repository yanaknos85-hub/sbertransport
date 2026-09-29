package ru.sber.transport.notifications.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.notifications.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.exceptions.NotAuthorizedException;
import ru.sber.transport.notifications.controller.NotificationController;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера работы с уведомлениями по лимитам
 */
@RestController
@RequiredArgsConstructor
class NotificationControllerImpl implements NotificationController {
    
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final LimitService limitService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationService notificationService;
    private final List<NotificationProcessor> notificationProcessors;
    private final ObjectMapper objectMapper;
    
    @Override
    public void notificationLimit(Authentication authentication, TransportTypeEnum type) throws JsonProcessingException {
        var name = authentication.getName();
        
        var userId = UUID.fromString(name);
        var employee = employeeService.getByUserId(userId).orElseThrow(() -> new NotAuthorizedException(userId));
        
        // если все ок, то по сотруднику найти подразделение
        var department  = departmentService.get(employee.getDepartmentId()).orElseThrow(() -> new EntityNotFoundException(Department.class, employee.getDepartmentId()));

        var organizationId = departmentService.get(department.getId()).orElseThrow().getOrganizationId();
        var currentLimit =
                limitService.get(department.getId(), type)
                            .orElseThrow(() -> new EntityNotFoundException(Limit.class, "Department ID " + department.getId()));
        var notificationClass = NotificationClass.LIMIT_DEPARTMENT;
        var notificationType = NotificationType.LOW_REMAINS_FROM_EMPLOYEE;
        
        var notificationSettings = notificationSettingsService.get(organizationId,
                notificationClass, notificationType);
        
        var notification = new Notification();
        notification.setSent(true);
        notification.setSettings(notificationSettings);
        notification.setEntity(objectMapper.writeValueAsString(currentLimit));
        notification.setReceiverId(employee.getId());
        notificationService.save(notification);
        notificationProcessors.forEach(NotificationProcessor::process);
        
    }
}
