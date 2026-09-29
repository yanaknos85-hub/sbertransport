package ru.sber.transport.trips.cargo.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.dispatcher.messages.IntegrationClientMessage;
import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messages.VehicleMessage;
import ru.sber.transport.trips.cargo.business.TripUseCases;
import ru.sber.transport.trips.cargo.message.EwbMessage;
import ru.sber.transport.trips.cargo.messaging.processor.ShiftProcessor;
import ru.sber.transport.trips.cargo.messaging.providers.AutoparkProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.IntegrationClientProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.providers.dispatcher.mapper.DispatcherMapper;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trips.cargo.providers.integration_client.mapper.IntegrationClientMapper;
import ru.sber.transport.trips.cargo.providers.vehicle.mapper.VehicleMapper;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Конфигурация слушателей.
 */
@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<RouteMessage>> requestCargoInput(TripUseCases<RouteMessage> tripUseCases) {
        return requestCargoInputSsl(tripUseCases);
    }

    @Bean
    Consumer<Message<ContractorMessage>> contractorInput(ContractorProvider contractorProvider, DispatcherMapper dispatcherMapper, DispatcherProvider dispatcherProvider) {
        return contractorInputSsl(contractorProvider, dispatcherMapper, dispatcherProvider);
    }

    @Bean
    Consumer<Message<ContractorUpdateTripMessage.Dispatcher>> dispatcherInput(DispatcherMapper dispatcherMapper, DispatcherProvider dispatcherProvider) {
        return dispatcherInputSsl(dispatcherMapper, dispatcherProvider);
    }

    @Bean
    Consumer<Message<DriverMessage>> driverInput(DriverProvider driverProvider, DriverMapper driverMapper) {
        return driverInputSsl(driverProvider, driverMapper);
    }

    @Bean
    Consumer<Message<AutoparkMessage>> autoparkInput(AutoparkProvider autoparkProvider) {
        return autoparkInputSsl(autoparkProvider);
    }

    @Bean
    Consumer<Message<VehicleMessage>> vehicleInput(VehicleProvider vehicleProvider, VehicleMapper vehicleMapper) {
        return vehicleInputSsl(vehicleProvider, vehicleMapper);
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInput(DriverProvider driverProvider, ShiftProcessor shiftProcessor) {
        return shiftInputSsl(driverProvider, shiftProcessor);
    }

    @Bean
    Consumer<Message<IntegrationClientMessage>> integrationClientInput(IntegrationClientProvider provider, IntegrationClientMapper mapper) {
        return integrationClientInputSsl(provider, mapper);
    }

    @Bean
    Consumer<Message<EwbMessage>> ewbInput(ShiftProcessor processor) {
        return ewbInputSsl(processor);
    }

    @Bean
    Consumer<Message<RouteMessage>> requestCargoInputSsl(TripUseCases<RouteMessage> tripUseCases) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            log.info("Handled a new cargo route...");
            tripUseCases.process(message, null, null);
        };
    }

    @Bean
    Consumer<Message<ContractorMessage>> contractorInputSsl(ContractorProvider contractorProvider, DispatcherMapper dispatcherMapper, DispatcherProvider dispatcherProvider) {
        return rawMessage -> {
            log.info("New contractor data received...");
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var message = rawMessage.getPayload();
            log.info("Saving contractor data...");
            contractorProvider.save(id, message);
            log.info("Contractor data saved");
            log.info("Saving main dispatcher data...");
            dispatcherProvider.save(dispatcherMapper.toModel(message.mainDispatcher(), message.id()));
            log.info("Main dispatcher data saved");
        };
    }

    @Bean
    Consumer<Message<ContractorUpdateTripMessage.Dispatcher>> dispatcherInputSsl(DispatcherMapper dispatcherMapper, DispatcherProvider dispatcherProvider) {
        return rawMessage -> {
            log.info("New dispatcher data received...");
            var message = rawMessage.getPayload();
            log.info("Saving dispatcher data...");
            dispatcherProvider.save(dispatcherMapper.toModel(message));
            log.info("Dispatcher data saved");
        };
    }

    @Bean
    Consumer<Message<AutoparkMessage>> autoparkInputSsl(AutoparkProvider provider) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            provider.save(message);
        };
    }

    @Bean
    Consumer<Message<DriverMessage>> driverInputSsl(DriverProvider driverProvider, DriverMapper driverMapper) {
        return rawMessage -> {
            log.info("New driver data received...");
            if(Source.TRIPS_CARGO.name().equals(rawMessage.getHeaders().get("source"))){
                log.warn("Driver data was not saved - current service is the source of message");
                return;
            }
            var message = rawMessage.getPayload();
            if (!message.driverSpeciality().equals("CARGO") && !message.driverSpeciality().equals("BOTH")) {
                log.warn("Driver speciality "+message.driverSpeciality()+" is not suitable. Data will be skipped.");
                return;
            }
            log.info("Saving driver data...");
            driverProvider.save(driverMapper.toModel(message));
            log.info("Driver data saved");
        };
    }

    @Bean
    Consumer<Message<VehicleMessage>> vehicleInputSsl(VehicleProvider vehicleProvider, VehicleMapper vehicleMapper) {
        return rawMessage -> {
            log.info("New vehicle data received...");
            var message = rawMessage.getPayload();
            log.info("Saving vehicle data...");
            vehicleProvider.save(vehicleMapper.toModel(message));
            log.info("Vehicle data saved");
        };
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInputSsl(DriverProvider driverProvider, ShiftProcessor shiftProcessor) {
        return rawMessage -> {
            log.info("New shift data received...");
            if(Source.TRIPS_CARGO.name().equals(rawMessage.getHeaders().get("source"))){
                log.warn("Shift data was not saved - current service is the source of message");
                return;
            }
            var message = rawMessage.getPayload();
            var driver = driverProvider.get(message.driverId());
            if (driver.isEmpty()) {
                log.warn("Driver not found. Make sure that the driver on the specified shift has type PASSENGER or BOTH.");
                return;
            }
            shiftProcessor.processShift(message, driver.get());
        };
    }

    @Bean
    Consumer<Message<IntegrationClientMessage>> integrationClientInputSsl(IntegrationClientProvider provider, IntegrationClientMapper mapper) {
        return rawMessage -> {
            log.debug("New integration client data received...");
            var payload = rawMessage.getPayload();
            provider.save(mapper.toModel(payload));
            log.debug("Integration client data saved");
        };
    }

    @Bean
    Consumer<Message<EwbMessage>> ewbInputSsl(ShiftProcessor processor) {
        return rawMessage -> {
            log.debug("New ewb data received...");
            var payload = rawMessage.getPayload();
            processor.processEwbUpdate(payload);
            log.debug("Ewb data saved");
        };
    }
}
