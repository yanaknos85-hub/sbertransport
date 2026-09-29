package ru.sber.transport.notifications.services.impl.commands.taxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

/**
 * Соответствует уведомлению notice_104 Назначение водителя
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaxiTripRequestDriverFoundNotificationCommand implements NotificationCommand<TripRequest> {
    private final DepartmentService departmentService;
    private final NotificationSettingsService notificationSettingsService;
    private final NotificationGlobalProcessor<TripRequest> processor;
    private final EmployeeService employeeService;
    private final TaxiTripService taxiTripService;
    
    @Override
    public boolean validate(TripRequest previous, TripRequest current) {
        if (previous == null || current == null) return false;
        return !previous.getStatus().equals(TripRequestStatus.TAXI_DRIVER_FOUND.name())
               && current.getStatus().equals(TripRequestStatus.TAXI_DRIVER_FOUND.name())
               && current.getTransportType().equals(TransportTypeEnum.TAXI);
    }
    
    @Override
    public void sendNotification(TripRequest request) throws JsonProcessingException {
        var passenger = employeeService.get(request.getPassengerId()).orElse(null);
    
        if (passenger != null && passenger.getDepartmentId() != null) {
            var department = departmentService.get(passenger.getDepartmentId()).orElseThrow();
            if (department.getDepartmentHeadId() != null && department.getOrganizationId() != null) {
                doProcess(request, passenger, department);
            } else {
                log.error("Некоторые данные департамента "+department.getId()+" не найдены!");
            }
        } else {
            log.error("Пассажир не найден!");
        }
    }

    private void doProcess(TripRequest request, Employee passenger, Department department) throws JsonProcessingException {
        TaxiTrip trip;
        if (request.getSharedRide() != null && request.getSharedRide().getId() != null) {
            trip = taxiTripService.getBySharedRideId(request.getSharedRide().getId());
            request.setTaxiTrip(trip);
        } else {
            trip = taxiTripService.getByRequestId(request.getId());
            if(trip!=null) {
                trip.setResolution(request.getTaxiTrip().getResolution());
                request.setTaxiTrip(trip);
            }
        }
        var notificationType = trip != null && (trip.getVehicle() == null || trip.getVehicle().empty()) ? NotificationType.DRIVER_ASSIGNED_XML : NotificationType.DRIVER_ASSIGNED;

        processor.process(passenger.getId(), notificationSettingsService.get(department.getOrganizationId(),
                NotificationClass.REQUEST_TAXI,
                notificationType), request);
    }
}