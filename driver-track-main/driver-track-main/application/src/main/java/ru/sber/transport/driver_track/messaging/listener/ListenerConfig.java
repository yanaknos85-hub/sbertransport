package ru.sber.transport.driver_track.messaging.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.service.DriverService;
import ru.sber.transport.driver_track.service.ExpectedWaypointsTripService;
import ru.sber.transport.trip.message.TripMessage;

import java.util.function.Consumer;

@Slf4j
@Configuration
@AllArgsConstructor
public class ListenerConfig {

    @Bean
    Consumer<Message<DriverMessage>> driverInput(DriverService driverService) {
        return rawMessage -> {
            var payload = rawMessage.getPayload();
            driverService.save(payload);
        };
    }

    @Bean
    Consumer<Message<DriverMessage>> driverInputSsl(DriverService driverService) {
        return rawMessage -> driverInput(driverService).accept(rawMessage);
    }

    @Bean
    Consumer<Message<TripMessage>> tripInput(ExpectedWaypointsTripService expectedWaypointsTripService) {
        return rawMessage -> {
            var payload = rawMessage.getPayload();
            expectedWaypointsTripService.save(payload);
        };
    }

    @Bean
    Consumer<Message<TripMessage>> tripInputSsl(ExpectedWaypointsTripService expectedWaypointsTripService) {
        return tripInput(expectedWaypointsTripService);
    }
}
