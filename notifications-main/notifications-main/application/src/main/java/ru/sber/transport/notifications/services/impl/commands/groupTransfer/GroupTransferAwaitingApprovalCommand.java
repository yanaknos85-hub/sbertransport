package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Соответствует уведомлению notice_4002
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupTransferAwaitingApprovalCommand implements NotificationCommand<TripApprove> {

    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripApprove> processor;
    private final EmployeeService employeeService;

    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return previous == null
                && current.getStatus() == null
                && current.getRequest().getTransportType().equals(TransportTypeEnum.GROUP_TRANSFER);
    }

    @Override
    public void sendNotification(TripApprove approve) {
        var notificationType = NotificationType.GROUP_TRANSFER_AWAITING_APPROVAL;

        var approvers = employeeService.getByIds(approve.getApproverIds());
        var passenger = employeeService.get(approve.getPassengerId()).orElseGet(() -> Employee.builder().firstName("Н/Д").build());
        var request = approve.getRequest();
        request.setPassenger(passenger);
        approvers = approvers.stream().distinct().toList();
        approvers.forEach(approver -> {
            if (approver != null && approver.getDepartmentId() != null) {
                var department = departmentService.get(approver.getDepartmentId()).orElseThrow();
                var organizationId = department.getOrganizationId();
                if (organizationId != null) {
                    try {
                        var settings = notificationSettingsService.get(organizationId,
                                NotificationClass.REQUEST_GROUP_TRANSFER,
                                notificationType);
                        processor.process(approver.getId(), settings, approve);
                    } catch (EntityNotFoundException | JsonProcessingException e) {
                        log.info(e.getMessage(), e);
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