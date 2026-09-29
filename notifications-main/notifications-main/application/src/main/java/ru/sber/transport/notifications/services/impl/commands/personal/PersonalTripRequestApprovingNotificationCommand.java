package ru.sber.transport.notifications.services.impl.commands.personal;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;

/**
 * Соответствует уведомлению notice_301 Согласование заявки
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PersonalTripRequestApprovingNotificationCommand implements NotificationCommand<TripApprove> {

    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripApprove> processor;
    private final EmployeeService employeeService;
    private final DeadlineSettingsService deadlineSettingsService;

    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return previous == null
                && current.getStatus() == null
                && current.getRequest().getTransportType().equals(TransportTypeEnum.PERSONAL);
    }

    @Override
    public void sendNotification(TripApprove approve) {
        var notificationType = NotificationType.APPROVE;

        var approvers = employeeService.getByIds(approve.getApproverIds());
        var passenger = employeeService.get(approve.getPassengerId()).orElseThrow();
        approve.getRequest().setPassenger(passenger);
        approvers.forEach(approver -> {
            if (approver != null && approver.getDepartmentId() != null) {
                var department = departmentService.get(approver.getDepartmentId()).orElseThrow();
                var organizationId = department.getOrganizationId();
                if (department.getOrganizationId() != null) {
                    deadlineSettingsService.findByOrganizationId(organizationId).ifPresent(deadlineSettings -> {
                        var requestDate = approve.getRequest().getCreationTime();
                        var duration = deadlineSettings.getPersonalAwaitingApprovalDeadline().getTotalDuration();
                        var deadline = requestDate.plus(duration);
                        approve.setDeadlineDateTime(deadline);
                    });
                    try {
                        processor.process(approver.getId(), notificationSettingsService.get(department.getOrganizationId(),
                                NotificationClass.REQUEST_PERSONAL,
                                notificationType), approve);
                    } catch (JsonProcessingException e) {
                        log.error("Processing failed", e);
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
