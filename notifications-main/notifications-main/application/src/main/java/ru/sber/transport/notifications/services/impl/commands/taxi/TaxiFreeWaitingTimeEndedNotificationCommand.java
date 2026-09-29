package ru.sber.transport.notifications.services.impl.commands.taxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxiFreeWaitingTimeEndedNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    private final TariffService<TaxiTariff> tariffService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null){
            return false;
        }
        return !previous.getStatus().equals(TripRequestStatus.TAXI_FREE_TIME_EXPIRED.name())
               && current.getStatus().equals(TripRequestStatus.TAXI_FREE_TIME_EXPIRED.name())
               && current.getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var notificationType = NotificationType.FREE_TIME_EXPIRED;
        
        var tariff = tariffService.findById(request.getTariffId());
        tariff.ifPresent(request::setTariff);
        
        var passenger = employeeService.get(request.getPassengerId()).orElseThrow();
        
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                        NotificationClass.REQUEST_TAXI,
                                                                             notificationType), request);
            } else {
                log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
            }
        } else {
            log.error("Пассажир не найден!");
        }
    }
}
