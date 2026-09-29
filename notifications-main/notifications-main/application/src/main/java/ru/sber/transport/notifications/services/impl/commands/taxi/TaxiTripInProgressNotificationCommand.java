package ru.sber.transport.notifications.services.impl.commands.taxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;

/**
 * Соответствует уведомлению notice_107 Поездка началась
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTripInProgressNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    private final TaxiTripService taxiTripService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        return previous != null
               && !previous.getStatus().equals(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
               && current.getStatus().equals(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
               && current.getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.TRIP_STARTED;
    
        var passenger = employeeService.get(request.getPassengerId()).orElse(null);
    
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                if (request.getSharedRide() != null
                    && request.getSharedRide().getId() != null) {
                    request.setTaxiTrip(
                            taxiTripService.getBySharedRideId(request.getSharedRide().getId()));
                } else {
                    request.setTaxiTrip(taxiTripService.getByRequestId(request.getId()));
                }
                processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.REQUEST_TAXI,
                                                                             notificationType), request);
            } else {
                log.error("Некоторые данные департамента %s не найдены!".formatted(department.getId()));
            }
        } else {
            log.error("Пассажир не найден!");
        }
    }
}