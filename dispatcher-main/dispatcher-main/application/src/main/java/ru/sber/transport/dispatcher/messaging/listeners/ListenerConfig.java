package ru.sber.transport.dispatcher.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.dto.enums.TripStatus;
import ru.sber.transport.dispatcher.mappers.DriverMapper;
import ru.sber.transport.dispatcher.mappers.ShiftMapper;
import ru.sber.transport.dispatcher.mappers.TripsMapper;
import ru.sber.transport.dispatcher.messages.*;
import ru.sber.transport.dispatcher.service.*;
import ru.sber.transport.dispatcher.validation.ShiftValidator;
import ru.sber.transport.trip.message.TripMessage;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

import java.util.function.Consumer;

/**
 * Конфигурация слушателей брокера.
 */
@Slf4j
@Configuration
class ListenerConfig {


    @Bean
    Consumer<Message<DriverMessage>> driverInput(DriverService driverService, DriverMapper driverMapper, ShiftRepository shiftRepository) {
        return driverInputSsl(driverService, driverMapper, shiftRepository);
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInput(ShiftService shiftService, ShiftMapper shiftMapper) {
        return shiftInputSsl(shiftService, shiftMapper);
    }

    @Bean
    Consumer<Message<TripMessage>> tripInput(TripsMapper tripsMapper, TripsService tripsService){
        return tripInputSsl(tripsMapper, tripsService);
    }

    @Bean
    Consumer<Message<UserDataConfirmationMessage>> confirmationDataInput(ContactConfirmationService contactConfirmationService) {
        return confirmationDataInputSsl(contactConfirmationService);
    }

    @Bean
    Consumer<Message<UserDataConfirmationMessage>> confirmationDataInputSsl(ContactConfirmationService contactConfirmationService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if(message.getId() != null && message.getPhone() != null) {
                contactConfirmationService.confirm(message);
            } else {
                log.warn("Received data is invalid");
            }
        };

    }

    @Bean
    Consumer<Message<DriverMessage>> driverInputSsl(DriverService driverService, DriverMapper driverMapper, ShiftRepository shiftRepository) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var existingDriverOpt = driverService.get(message.getId());
            if(existingDriverOpt.isPresent()) {
                var existingDriver = existingDriverOpt.get();
                existingDriver.setOnline(message.online());
                driverService.save(existingDriver);
            }
        };
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInputSsl(ShiftService shiftService, ShiftMapper shiftMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            Source source = Source.CONTRACTOR;
            var sourceValue = rawMessage.getHeaders().get("source");
            if(sourceValue != null){
                source = Source.valueOf(sourceValue.toString());
            }
            var existingShiftOpt = shiftService.get(message.getId());
            if(existingShiftOpt.isPresent()) {
                var existingShift = existingShiftOpt.get();
                shiftMapper.update(existingShift, message);
                shiftService.save(existingShift, source);
            }
        };
    }

    @Bean
    Consumer<Message<TripMessage>> tripInputSsl(TripsMapper tripsMapper, TripsService tripsService){
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if(TripStatus.ORDER_CANCELLED_BY_CLIENT.name().equals(message.getStatus())
                || TripStatus.ORDER_CANCELLED_BY_DRIVER.name().equals(message.getStatus())
                    || TripStatus.ORDER_EXPIRED.name().equals(message.getStatus())){
                tripsService.delete(message.getId());
            } else {
                tripsService.save(tripsMapper.toModel(message));
            }
        };
    }

    @Bean
    Consumer<Message<TransportMessage>> transportInput( VehicleService service) {
        return transportInputSsl(service);
    }

    @Bean
    Consumer<Message<TransportMessage>> transportInputSsl(VehicleService service) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            service.upsert(message);
        };
    }

    @Bean
    Consumer<Message<ShiftFromMaisMessage>> integrationMaisShiftInput(ShiftService service) {
        return integrationMaisShiftInputSsl(service);
    }

    @Bean
    Consumer<Message<ShiftFromMaisMessage>> integrationMaisShiftInputSsl(ShiftService shiftService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (ShiftValidator.isAcceptable(message)) {
                shiftService.handleShiftFromMais(message);
            }
        };
    }

    @Bean
    Consumer<Message<EwbMessage>> ewbInput(ShiftControllerService service) {
        return ewbInputSsl(service);
    }

    @Bean
    Consumer<Message<EwbMessage>> ewbInputSsl(ShiftControllerService shiftService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            shiftService.sendSocketForEwb(message);
        };
    }


}
