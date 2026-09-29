package ru.sber.transport.notifications.services.impl.commands.personal;

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
 * Соответствует уведомлению notice_306 Прибытие в промежуточный пункт
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalTripWaypointArrivedNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    
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
        return current.getStatus().equals(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
               && current.getTransportType().equals(TransportTypeEnum.PERSONAL)
               && previousCount < currentCount;
    }
    
    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.WAYPOINT_ARRIVED;
    
        var passenger = employeeService.get(request.getPassengerId()).orElse(null);
    
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getOrganizationId() != null) {
                processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.REQUEST_PERSONAL,
                                                                                 notificationType), request);
            } else {
                log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
            }
        } else {
            log.error("Пассажир не найден!");
        }
    }
}