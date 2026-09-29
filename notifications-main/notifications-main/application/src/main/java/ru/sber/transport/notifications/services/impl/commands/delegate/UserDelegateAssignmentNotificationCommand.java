package ru.sber.transport.notifications.services.impl.commands.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.coprorate.Delegate;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sber.transport.notifications.services.NotificationCommand;

/**
 * Соответствует уведомлению notice_1101 Назначение делегата
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDelegateAssignmentNotificationCommand implements NotificationCommand<Delegate> {
    
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<Delegate> processor;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    
    @Override
    public boolean validate(Delegate previous, Delegate current) {
        return previous == null && current != null;
    }
    
    @Override
    public void sendNotification(Delegate delegate) throws JsonProcessingException {
        var notificationType = NotificationType.ASSIGNMENT;
    
        var employeeDelegate = employeeService.get(delegate.getDelegateId()).orElseThrow();
        
        if (employeeDelegate.getDepartmentId() != null) {
            var department = departmentService.get(employeeDelegate.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                processor.process(employeeDelegate.getId(), notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.USER_DELEGATE,
                                                                                    notificationType), delegate);
            } else {
                    log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
            }
        } else {
                log.error("Департамент делегата с ID "+delegate.getId()+" не найден!");
        }
    }
}
