package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.TripRequestService;

import java.util.NoSuchElementException;

/**
 * Реализация процессора согласований поездок.
 */
@Slf4j
@EnableAsync
@RequiredArgsConstructor
@Component("approveProcessor")
class TripApproveProcessorImpl extends BaseProcessor<TripApprove> {

    private final NotificationSettingsService notificationSettingsService;

    private final DepartmentService departmentService;

    private final EmployeeService employeeService;

    private final TripRequestService tripRequestService;

    @Override
    public void process(TripApprove approve) {
        try {
            var request = tripRequestService.get(approve.getRequestId());

            approve.setRequest(request);
            approve.setApprover(employeeService.get(approve.getApproverId()).orElseThrow());
            if(approve.getApproveStatus() == null) {
                if (approve.getStatus() == null) {
                    createApproverNotification(approve);
                } else {
                    createPassengerNotification(approve);
                }
            }
            else {
                createApproverTripNotification(approve);
            }
        } catch (NoSuchElementException | JsonProcessingException e) {
            log.debug(String.format("There is no request with ID %s receive yet", approve.getRequestId()));
        }
    }

    private void createApproverTripNotification(TripApprove approve) throws JsonProcessingException {
        var notificationType = switch (approve.getApproveStatus()) {
            case APPROVED -> NotificationType.COOP_TRIP_ATTACHMENT_APPROVE;
            case DECLINED -> NotificationType.COOP_TRIP_ATTACHMENT_STATUS;
            default -> null;
        };

        var approver = approve.getApprover();
        var passenger = approve.getPassenger();

        var departmentApproverId = approver.getDepartmentId();
        var organizationApproverId = departmentService.get(departmentApproverId).orElseThrow().getOrganizationId();
        process(approver.getId(), notificationSettingsService.get(organizationApproverId,
                NotificationClass.REQUEST_PERSONAL,
                                                          notificationType), approve);
        if(passenger != null) {
            var departmentPassengerId = passenger.getDepartmentId();
            var organizationPassengerId = departmentService.get(departmentPassengerId).orElseThrow().getOrganizationId();
            process(passenger.getId(), notificationSettingsService.get(organizationPassengerId,
                    NotificationClass.REQUEST_PERSONAL,
                                                               notificationType), approve);
        }
    }

    private void createPassengerNotification(TripApprove approve) throws JsonProcessingException {
        var passengerId = approve.getRequest().getPassengerId();
        var passenger = employeeService.get(passengerId).orElseThrow();
        var departmentId = passenger.getDepartmentId();
        var organizationId = departmentService.get(departmentId).orElseThrow().getOrganizationId();
        var transportType = approve.getRequest().getTransportType();
        if (transportType == null) {
            log.warn(String.format("Transport type for request ID %s not found", approve.getRequestId()));
        }
        var notificationClass = defineNotificationType(transportType);
        var settings = notificationSettingsService.get(organizationId, notificationClass,
                                                       NotificationType.APPROVE_STATUS);
        process(approve.getRequest().getPassengerId(), settings, approve);
    }

    private void createApproverNotification(TripApprove approve) throws JsonProcessingException {
        var approver = approve.getApprover();
        var departmentId = approver.getDepartmentId();
        var organizationId = departmentService.get(departmentId).orElseThrow().getOrganizationId();
        var transportTypeEnum = approve.getRequest().getTransportType();
        var notificationClass = defineNotificationType(transportTypeEnum);
        var settings = notificationSettingsService.get(organizationId,
                notificationClass, NotificationType.APPROVE);
        process(approve.getApprover().getId(), settings, approve);
    }

    private NotificationClass defineNotificationType(TransportTypeEnum transportTypeEnum) {
        return switch (transportTypeEnum) {
            case TAXI -> NotificationClass.REQUEST_TAXI;
            case PERSONAL -> NotificationClass.REQUEST_PERSONAL;
            case PUBLIC -> NotificationClass.REQUEST_PUBLIC;
            case CARSHARING -> NotificationClass.REQUEST_CAR_SHARING;
            case BICYCLE -> NotificationClass.REQUEST_BICYCLE;
            case SCOOTER -> NotificationClass.REQUEST_SCOOTER;
            default -> throw new IllegalArgumentException(String.format("There is no notification type for request with type" +
                    " %s", transportTypeEnum));
        };
    }
}
