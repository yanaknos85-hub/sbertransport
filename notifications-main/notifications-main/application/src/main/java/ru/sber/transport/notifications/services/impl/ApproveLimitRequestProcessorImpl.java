package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationService;

import jakarta.transaction.Transactional;
import java.util.UUID;

@Slf4j
@EnableAsync
@RequiredArgsConstructor
@Component
class ApproveLimitRequestProcessorImpl extends BaseProcessor<ApproveLimitRequest>{
    
    private final NotificationService notificationService;
    private final EmployeeService employeeService;
    private final NotificationSettingsService settingsService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void process(ApproveLimitRequest data) throws JsonProcessingException {
        UUID employeeId = data.getEmployeeId();
        UUID authorId = data.getAuthorId();
        var employee = employeeService.get(employeeId).orElseThrow();
        var author = employeeService.get(authorId).orElseThrow();
        var notificationClass = data.getLimitType().equals(LimitType.DEPARTMENT) ?
                                NotificationClass.REQUEST_LIMIT_DEPARTMENT : NotificationClass.REQUEST_LIMIT_PERSON;
        var status = data.getApprovalState();

        var notificationType = switch (status) {
            case AWAITING_APPROVAL -> NotificationType.APPROVE;
            case APPROVED -> NotificationType.REQUEST_EXECUTION;
            case DECLINED -> NotificationType.APPROVE_STATUS;
        };
        createProcess(data, employee, notificationClass, notificationType);
        createProcess(data, author, notificationClass, notificationType);
    }
    private void createProcess(ApproveLimitRequest data, Employee employee, NotificationClass notificationClass,
                               NotificationType notificationType) throws JsonProcessingException {
        var notificationSettings = settingsService.get
                (data.getOrganizationId(), notificationClass, notificationType);
        var notification = new Notification();
        notification.setReceiverId(employee.getId());
        notification.setSent(true);
        notification.setSettings(notificationSettings);
        notification.setEntity(objectMapper.writeValueAsString(data));
        notificationService.save(notification);
        process(employee.getId(), notificationSettings, data);
    }
}
