package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.Processor;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.util.LinkedList;
import java.util.List;

@EnableAsync
@Slf4j
@RequiredArgsConstructor
@Component("taxiTripProcessor")
class TaxiTripProcessorImpl implements Processor<TaxiTrip> {

    private final NotificationSettingsService settingsService;

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final List<NotificationProcessor> processorList;

    private final TripRequestRepository requestRepository;

    @Async
    @Transactional
    @Override
    public void process(TaxiTrip data) throws JsonProcessingException {
        log.debug("New trip handled...");
        //Вызов метода отправки сообщения пассажиру закомментирован, т.к. он провоцирует повторную отправку уведомления
        //Было решено не выпиливать метод и сервис т.к. в будущем может потребоваться доработка уведомлений интеграции по АПИ
        //sendPassenger(data);
    }

    private void sendPassenger(TaxiTrip data) throws JsonProcessingException {
        var driver = data.getDriver();
        var vehicle = data.getVehicle();
        var notificationClass = NotificationClass.REQUEST_TAXI;
        var notificationType = (driver == null && vehicle == null) ? NotificationType.DRIVER_ASSIGNED_XML : NotificationType.DRIVER_ASSIGNED_DISPATCHER;
        var requests = new LinkedList<TripRequest>();
        if (InboundTaxiTripStatus.DRIVER_ASSIGNED.equals(data.getStatus())) {
            if (data.getSharedRideId() != null) {
                requests.addAll(requestRepository.findAllBySharedRideId(data.getSharedRideId()));
            } else {
                requestRepository.findById(data.getRequestId()).ifPresent(requests::add);
            }
            var passengers = requests.stream().map(TripRequest::getPassengerId).toList();
            for (var passengerId : passengers) {
                var notificationSettings = settingsService.get(data.getOrganizationId(),
                        notificationClass, notificationType);
                var notification = notificationService.find(data.getId()).orElseGet(Notification::new);

                log.info("Driver assigned by trip handled. Send to passenger");
                data.setDriver(driver);
                data.setVehicle(vehicle);

                notification.setReceiverId(passengerId);
                notification.setSent(true);
                notification.setSettings(notificationSettings);
                notification.setEntity(objectMapper.writeValueAsString(data));
                notification.setEntityId(data.getId());
                var saved = notificationService.save(notification);
                processorList.forEach(processor -> processor.process(saved));
            }
        }
    }
}
