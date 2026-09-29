package ru.sber.transport.notifications.services.impl.commands.taxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationCommand;

/**
 * Соответствует уведомлению notice_111 Присоединение к совместной поездке
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaxiCoopTripAttachmentNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous != null || current == null || current.getSharedRide() == null
            || current.getSharedRide().getRequestIds() == null) {
            return false;
        }
        int currentCount = current.getSharedRide().getRequestIds().size();
        return currentCount > 0
               && current.getStatus().equals(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
               && current.getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripRequest request) {
        var notificationType = NotificationType.COOP_TRIP_ATTACHMENT;
    
        var passengers = employeeService.getPassengersByRequestIds(request.getSharedRide().getRequestIds());
        var passengerFromNewRequest = employeeService.get(request.getPassengerId()).orElseThrow();
        request.setPassenger(passengerFromNewRequest);
        passengers.forEach(passenger -> {
            if (passenger.getDepartmentId() != null) {
                var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
                if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                    try {
                        processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                                NotificationClass.REQUEST_TAXI,
                                                                                      notificationType), request);
                    } catch (JsonProcessingException e) {
                        log.error("Processing failed", e);
                    }
                } else {
                    log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
                }
            } else {
                log.error("Департамент пассажира "+passenger.getId()+" не найден!");
            }
        });
    }
}