package ru.sber.transport.trip.messaging.senders.dispatcher.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.dto.TripV2Dto;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.web_socket.handlers.WebSocketHandler;

/**
 * Реализация отправителя.
 */
@SkipConsentCheck("/ws/")
@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
@NoAuthorize("/ws/**")
public class TripDispatcherSender extends WebSocketHandler<TripV2Dto> implements TripSender {

    private final ContractorProvider contractorProvider;

    private final DispatcherProvider dispatcherProvider;

    private final DriverProvider driverProvider;

    private final VehicleProvider vehicleProvider;

    private final TripMapper tripMapper;

    private final ru.sber.transport.trip.messaging.senders.TripSender tripSender;

    @Qualifier("contractorTripUpdateOutput")
    private final ObjectProvider<OutputBridge> contractorTripUpdateOutput;

    @Qualifier("contractorTripUpdateOutputSsl")
    private final ObjectProvider<OutputBridge> contractorTripUpdateOutputSsl;

    @Override
    public void send(Trip trip, boolean isNew, ChannelType... channels) {
        for (var channel : channels) {
            if (ChannelType.BOTH.equals(channel) || ChannelType.KAFKA.equals(channel)) {
                sendToKafka(trip);
            }
            if (ChannelType.BOTH.equals(channel) || ChannelType.WEB_SOCKET.equals(channel)) {
                trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP,
                        contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
                sendToWebsoket(trip, isNew);
            }
        }
        if (channels.length == 0) {
            log.warn("Argument 'channels' is empty. No messages sent");
        }
    }

    @Override
    public String url() {
        return "/trips/v2/*";
    }

    private void sendToKafka(Trip trip) {
        try {
            var tripDriver = driverProvider.get(trip.getDriverId()).orElse(null);
            var tripDispatcher = dispatcherProvider.get(trip.getDispatcherId()).orElse(null);
            var tripVehicle = vehicleProvider.get(trip.getVehicleId()).orElse(null);
            var message = tripMapper.toUpdateMessage(trip, tripDriver, tripDispatcher, tripVehicle);

            contractorTripUpdateOutput.ifAvailable(ob -> ob.send(message));
            contractorTripUpdateOutputSsl.ifAvailable(ob -> ob.send(message));

            tripSender.send(trip);
            log.debug("Message sent with Kafka");
        } catch (Exception e) {
            log.error("sendToKafka: Error!", e);
        }
    }

    private void sendToWebsoket(Trip trip, boolean isNew) {
        var authorizedSessionsIds = getAuthorizedSessionsIds();
        if(authorizedSessionsIds.isEmpty()) {
            log.debug("Authorized sessions is empty, sending cancelled");
        } else {
            try {
                var dispatchers = dispatcherProvider.findAllByContractorIdAndIdIn(trip.getContractorId(), authorizedSessionsIds);
                log.debug("Sending to {} dispatchers", dispatchers.size());
                var tripDriver = driverProvider.get(trip.getDriverId()).orElse(null);
                var tripDispatcher = dispatcherProvider.get(trip.getDispatcherId()).orElse(null);
                var message = tripMapper.toDtoForSocketV2(trip, tripDriver, tripDispatcher, isNew);
                for (var dispatcher : dispatchers) {
                    send(dispatcher.getId(), message);
                }
                if (tripDriver != null && authorizedSessionsIds.contains(tripDriver.getId())) {
                    if (trip.getStatus() == TripStatus.DRIVER_ASSIGNED) {
                        send(tripDriver.getId(), tripMapper.copyDtoForSocketV2WithIsNew(message, true));
                    } else {
                        send(tripDriver.getId(), message);
                    }
                }
                log.debug("Message sent with WebSocket");
            } catch (Exception e) {
                log.error("sendToWebsoket: Error!", e);
            }
        }
    }
}
