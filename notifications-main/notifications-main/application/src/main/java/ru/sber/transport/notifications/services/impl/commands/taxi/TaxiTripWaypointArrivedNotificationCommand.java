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
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;

/**
 * Соответствует уведомлению notice_108 Прибытие в промежуточный пункт
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTripWaypointArrivedNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    private final TariffService<TaxiTariff> tariffService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) {
            return false;
        }
        long previousCount =
                previous.getWaypoints().stream()
                        .filter(waypoint -> (waypoint.getCheckinAutomatic() != null && waypoint.getCheckinAutomatic())
                                            || (waypoint.getCheckinManual() != null && waypoint.getCheckinManual()))
                        .count();
        long currentCount =
                current.getWaypoints().stream()
                       .filter(waypoint -> (waypoint.getCheckinAutomatic() != null && waypoint.getCheckinAutomatic())
                                           || (waypoint.getCheckinManual() != null && waypoint.getCheckinManual()))
                       .count();
        return current.getStatus().equals(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
               && current.getTransportType().equals(TransportTypeEnum.TAXI)
               && previousCount < currentCount;
    }
    
    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.WAYPOINT_ARRIVED;
    
        var passenger = employeeService.get(request.getPassengerId()).orElse(null);
    
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                var tariffOpt = tariffService.findById(request.getTariffId());
                if (tariffOpt.isPresent()) {
                    request.setTariff(tariffOpt.get());
                    processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                            NotificationClass.REQUEST_TAXI,
                                                                                 notificationType), request);
                } else {
                    log.error("Tariff data for request {} not found!", request.getId());
                }
            } else {
                log.error("Several data of department {} not found!", department.getId());
            }
        } else {
            log.error("Passenger not found!");
        }
    }
}