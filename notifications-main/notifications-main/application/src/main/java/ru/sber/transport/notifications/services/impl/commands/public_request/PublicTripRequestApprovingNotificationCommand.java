package ru.sber.transport.notifications.services.impl.commands.public_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;

/**
 * Соответствует уведомлению notice_201 Согласование заявки
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PublicTripRequestApprovingNotificationCommand implements NotificationCommand<TripApprove> {

    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripApprove> processor;
    private final EmployeeService employeeService;
    private final DeadlineSettingsService deadlineSettingsService;

    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return previous == null
                && current.getStatus() == null
                && current.getRequest().getTransportType().equals(TransportTypeEnum.PUBLIC);
    }

    @Override
    public void sendNotification(TripApprove approve) {
        var notificationType = NotificationType.APPROVE;

        var approvers = employeeService.getByIds(approve.getApproverIds());
        var passenger = employeeService.get(approve.getPassengerId()).orElseThrow();
        approve.getRequest().setPassenger(passenger);
        approvers.forEach(approver -> {
            if (approver.getDepartmentId() != null) {
                var department = departmentService.get(approver.getDepartmentId()).orElseThrow();
                var organizationId = department.getOrganizationId();
                if (organizationId != null) {
                    deadlineSettingsService.findByOrganizationId(organizationId).ifPresent(deadlineSettings -> {
                        var requestDate = approve.getRequest().getCreationTime();
                        var duration = deadlineSettings.getPublicAwaitingApprovalDeadline().getTotalDuration();
                        var deadline = requestDate.plus(duration);
                        approve.setDeadlineDateTime(deadline);
                    });
                    try {
                        processor.process(approver.getId(), notificationSettingsService.get(organizationId,
                                NotificationClass.REQUEST_PUBLIC,
                                notificationType), approve);
                    } catch (EntityNotFoundException | JsonProcessingException e) {
                        log.warn("There is no notification settings for organization {} for " +
                                        "notification {} typed as {}",
                                organizationId, NotificationClass.REQUEST_PUBLIC, notificationType);
                    }
                } else {
                    log.error("Некоторые данные департамента " + department.getId() + " не найдены!");
                }
            } else {
                log.error("Департамент согласующего не найден!");
            }
        });
    }
}
