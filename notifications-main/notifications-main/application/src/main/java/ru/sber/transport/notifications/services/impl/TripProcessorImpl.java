package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.dao.VehicleRepository;
import ru.sber.transport.notifications.database.dao.messages.contractor.DriverRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.enums.TripStatus;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.Processor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@EnableAsync
@Slf4j
@RequiredArgsConstructor
@Component("tripProcessor")
class TripProcessorImpl implements Processor<Trip> {

    private final NotificationSettingsService settingsService;

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final List<NotificationProcessor> processorList;

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Async
    @Transactional
    @Override
    public void process(Trip data) throws JsonProcessingException {
        log.info("New trip handled...");
        sendDriver(data);
        sendPlannedDriver(data);
    }

    @Nullable
    private void sendDriver(Trip data) throws JsonProcessingException {
        var notificationClass = NotificationClass.CONTRACTOR;
        var notificationType = NotificationType.DRIVER_ASSIGNED;
        var driverId = Optional.ofNullable(data.getDriver()).map(Driver::getId).orElse(null);
        if (driverId != null && TripStatus.DRIVER_ASSIGNED.name().equals(data.getStatus())) {
            var notificationSettings = settingsService.get(data.getDriver().getContractorId(),
                    notificationClass, notificationType);

            var notification = notificationService.find(data.getId()).orElseGet(Notification::new);
            var tripToSend = notification.getEntityFromJson(Trip.class);
            var existsDriver = Optional.ofNullable(tripToSend)
                    .map(Trip::getDriver)
                    .map(Driver::getId).orElse(null);
            if (!Objects.equals(driverId, existsDriver)) {
                log.info("Driver assigned by trip handled");
                notification.setReceiverId(driverId);
                notification.setSent(true);
                notification.setSettings(notificationSettings);
                notification.setEntity(objectMapper.writeValueAsString(data));
                notification.setEntityId(data.getId());
                var saved = notificationService.save(notification);
                processorList.forEach(processor -> processor.process(saved));
            }
        }
    }

    private void sendPlannedDriver(Trip data) throws JsonProcessingException {
        var notificationClass = NotificationClass.CONTRACTOR;
        var notificationType = NotificationType.DRIVER_ASSIGNED;

        var plannedShiftId = data.getPlannedShiftId();
        var driverId = Optional.ofNullable(data.getDriver()).map(Driver::getId).orElse(null);

        var notification = notificationService.find(data.getId()).orElseGet(Notification::new);
        var tripToSend = notification.getEntityFromJson(Trip.class);
        var existsPlannedShiftId = Optional.ofNullable(tripToSend)
                .map(Trip::getPlannedShiftId).orElse(null);
        if (plannedShiftId != null && !plannedShiftId.equals(existsPlannedShiftId)) {
            var notificationSettings = settingsService.get(data.getDriver().getContractorId(),
                    notificationClass, notificationType);
            log.info("Driver planned on trip handled");
            notification.setReceiverId(driverId);
            notification.setSent(true);
            notification.setSettings(notificationSettings);
            notification.setEntity(objectMapper.writeValueAsString(data));
            notification.setEntityId(data.getId());
            var saved = notificationService.save(notification);
            processorList.forEach(processor -> processor.process(saved));
        }
    }
}
