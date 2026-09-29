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
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationCommand;

/**
 * Соответствует уведомлению notice_102 Статус согласования заявки
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiTripRequestApprovingStatusNotificationCommand implements NotificationCommand<TripApprove> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripApprove> processor;
    private final EmployeeService employeeService;
    
    @Override
    public boolean validate(TripApprove previous, TripApprove current) {
        return previous != null
               && previous.getStatus() == null
               && current.getStatus() != null
               && current.getRequest().getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripApprove approve) throws JsonProcessingException {
        var notificationType = NotificationType.APPROVE_STATUS;
        var passenger = employeeService.get(approve.getPassengerId()).orElse(null);
    
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.REQUEST_TAXI,
                                                                             notificationType), approve);
            } else {
                log.error("Некоторые данные департамента %s не найдены!".formatted(department.getId()));
            }
        } else {
            log.error("Пассажир не найден!");
        }
    }
}