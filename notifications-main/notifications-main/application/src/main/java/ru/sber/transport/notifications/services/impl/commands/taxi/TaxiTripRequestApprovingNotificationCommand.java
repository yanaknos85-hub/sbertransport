package ru.sber.transport.notifications.services.impl.commands.taxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

/**
 * Соответствует уведомлению notice_101 Согласование заявки
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaxiTripRequestApprovingNotificationCommand implements NotificationCommand<TripApprove> {
    
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripApprove> processor;
    private final EmployeeService employeeService;
    private final DeadlineSettingsService deadlineSettingsService;
    
    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return previous == null
               && current.getStatus() == null
               && current.getRequest().getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripApprove approve) {
        var notificationType = NotificationType.APPROVE;
    
        var approvers = employeeService.getByIds(approve.getApproverIds());
        var passenger = employeeService.get(approve.getPassengerId()).orElseGet(() -> Employee.builder().firstName("Н/Д").build());
        var request = approve.getRequest();
        request.setPassenger(passenger);
        approvers.forEach(approver -> {
            if (approver != null && approver.getDepartmentId() != null) {
                var department = departmentService.get(approver.getDepartmentId()).orElseThrow();
                var organizationId = department.getOrganizationId();
                if (department.getDepartmentHeadId() != null && organizationId != null) {
                    deadlineSettingsService.findByOrganizationId(organizationId)
                            .ifPresent(settings -> {
                                var duration = settings.getTaxiAwaitingApprovalDeadline().getTotalDuration();
                                var creationTime = request.getCreationTime();
                                approve.setDeadlineDateTime(creationTime.plus(duration));
                            });
                    try {
                        var settings = notificationSettingsService.get(organizationId,
                                NotificationClass.REQUEST_TAXI,
                                                                           notificationType);
                        processor.process(approver.getId(), settings, approve);
                    } catch (EntityNotFoundException | JsonProcessingException e) {
                        log.info(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
                }
            } else {
                log.error("Департамент согласующего не найден!");
            }
        });
    }
}
