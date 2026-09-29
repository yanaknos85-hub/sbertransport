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

import java.util.stream.Collectors;

/**
 * Соответствует уведомлению notice_313 Изменение в совместной поездке
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalCoopTripChangesNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null || current.getSharedRide() == null
            || current.getSharedRide().getRequestIds() == null) return false;
        int currentCount = current.getSharedRide().getRequestIds().size();
        return currentCount > 1
               && current.getStatus().equals(TripRequestStatus.PERSONAL_CANCELLED.name())
               && current.getTransportType().equals(TransportTypeEnum.PERSONAL);
    }
    
    @Override
    public void sendNotification(TripRequest request) {
        var notificationType = NotificationType.COOP_TRIP_CHANGES;
    
        var passengersInSharedRequest =
                employeeService.getPassengersByRequestIds(request.getSharedRide()
                                                                 .getRequestIds()).stream()
                               .filter(employee -> !employee.getId().equals(request.getPassengerId()) && employee.getDepartmentId() != null)
                               .collect(Collectors.toSet());
        var passengerInCurrentRequest = employeeService.get(request.getPassengerId()).orElseThrow();
        if (!passengersInSharedRequest.isEmpty()) {
            passengersInSharedRequest.forEach(passenger -> {
                request.setPassenger(passengerInCurrentRequest);
                var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
                if (department.getOrganizationId() != null) {
                    try {
                        processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                                NotificationClass.REQUEST_PERSONAL,
                                                                                     notificationType), request);
                    } catch (JsonProcessingException e) {
                        log.error("Processing failed", e);
                    }
                } else {
                    log.error("Некоторые данные департамента " + department.getId() +
                              " для пассажира " + passenger.getId() + " не найдены!");
                }
            });
        } else {
            log.error("Пассажиры не найдены!");
        }
    }
}