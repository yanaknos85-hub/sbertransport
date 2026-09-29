package ru.sber.transport.trip.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.dispatcher.messages.*;
import ru.sber.transport.driver_track.messaging.TripFactDistanceMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.trip.business.TripUseCases;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.message.EwbMessage;
import ru.sber.transport.trip.messaging.mapper.RequestMapper;
import ru.sber.transport.trip.messaging.processor.ShiftProcessor;
import ru.sber.transport.trip.messaging.providers.*;
import ru.sber.transport.trip.providers.dispatcher.mapper.DispatcherMapper;
import ru.sber.transport.trip.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trip.providers.integration_client.mapper.IntegrationClientMapper;
import ru.sber.transport.trip.providers.vehicle.mapper.VehicleMapper;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Конфигурация слушателей.
 */
@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<RequestMessage>> requestInput(TripUseCases<Request> tripUseCases, RequestMapper requestMapper, ContractorProvider contractorProvider) {
        return requestInputSsl(tripUseCases, requestMapper, contractorProvider);
    }

    @Bean
    Consumer<Message<ContractorMessage>> contractorInput(ContractorProvider contractorProvider, DispatcherProvider dispatcherProvider, DispatcherMapper dispatcherMapper) {
        return contractorInputSsl(contractorProvider, dispatcherProvider, dispatcherMapper);
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
    Consumer<Message<VehicleMessage>> vehicleInput(VehicleProvider vehicleProvider, VehicleMapper vehicleMapper) {
        return vehicleInputSsl(vehicleProvider, vehicleMapper);
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInput(DriverProvider driverProvider, ShiftProcessor shiftProcessor) {
        return shiftInputSsl(driverProvider, shiftProcessor);
    }

    @Bean
    Consumer<Message<TripFactDistanceMessage>> factDistanceInput(RequestService requestService) {
        return factDistanceInputSsl(requestService);
    }

    @Bean
    Consumer<Message<IntegrationClientMessage>> integrationClientInput(IntegrationClientProvider provider, IntegrationClientMapper mapper) {
        return integrationClientInputSsl(provider, mapper);
    }

    @Bean
    Consumer<Message<AutoparkMessage>> autoparkInput(AutoparkProvider provider) {
        return autoparkInputSsl(provider);
    }

    @Bean
    Consumer<Message<EwbMessage>> ewbInput(ShiftProcessor processor) {
        return ewbInputSsl(processor);
    }

    @Bean
    Consumer<Message<AutoparkMessage>> autoparkInputSsl(AutoparkProvider provider) {
        return rawMessage -> {
          var message = rawMessage.getPayload();
          provider.save(message);
        };
    }

    @Bean
    Consumer<Message<RequestMessage>> requestInputSsl(TripUseCases<Request> tripUseCases, RequestMapper requestMapper, ContractorProvider contractorProvider) {
        return rawMessage -> {
            if (rawMessage.getHeaders().containsKey("transportType") && (!Objects.equals(rawMessage.getHeaders().get("transportType"), "TAXI") && !Objects.equals(rawMessage.getHeaders().get("transportType"), "GROUP_TRANSFER"))) {
                return;
            }
            var message = rawMessage.getPayload();
            if (!contractorProvider.isDispatcher(message.getContractorId())) {
                log.debug("Request contractor integration type is not dispatcher, request data not saved");
                return;
            }
            log.debug("Handled a new request data...");
            var tripRequestStatus = TripRequestStatus.valueOf(message.getStatus());
            if ((TransportTypeEnum.TAXI.name().equals(message.getTransportType())
                    || TransportTypeEnum.GROUP_TRANSFER.name().equals(message.getTransportType()))
                    && (!tripRequestStatus.isApprovable() || tripRequestStatus.isTerminal())) {
                log.debug("Handled a new approved request");
                var request = requestMapper.toModel(message);
                tripUseCases.process(request, message.getDriverId(), message.getVehicleId());
            }
        };
    }

    @Bean
    Consumer<Message<ContractorMessage>> contractorInputSsl(ContractorProvider contractorProvider, DispatcherProvider dispatcherProvider, DispatcherMapper dispatcherMapper) {
        return rawMessage -> {
            log.debug("New contractor data received...");
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var message = rawMessage.getPayload();
            log.debug("Saving contractor data...");
            contractorProvider.save(id, message);
            log.debug("Contractor data saved");
            log.debug("Saving main dispatcher data...");
            dispatcherProvider.save(dispatcherMapper.toModel(message.mainDispatcher(), message.id()));
            log.debug("Main dispatcher data saved");
        };
    }

    @Bean
    Consumer<Message<ContractorUpdateTripMessage.Dispatcher>> dispatcherInputSsl(DispatcherMapper dispatcherMapper, DispatcherProvider dispatcherProvider) {
        return rawMessage -> {
            log.debug("New dispatcher data received...");
            var message = rawMessage.getPayload();
            log.debug("Saving dispatcher data...");
            dispatcherProvider.save(dispatcherMapper.toModel(message));
            log.debug("Dispatcher data saved");
        };
    }

    @Bean
    Consumer<Message<DriverMessage>> driverInputSsl(DriverProvider driverProvider, DriverMapper driverMapper) {
        return rawMessage -> {
            log.debug("New driver data received...");
            if (Source.TRIPS.name().equals(rawMessage.getHeaders().get("source"))) {
                log.debug("Driver data was not saved - current service is the source of message");
                return;
            }
            var message = rawMessage.getPayload();
            if (!message.driverSpeciality().equals("PASSENGER") && !message.driverSpeciality().equals("BOTH")) {
                log.debug("Driver speciality " + message.driverSpeciality() + " is not suitable. Data will be skipped.");
                return;
            }
            log.debug("Saving driver data...");
            driverProvider.save(driverMapper.toModel(message));
            log.debug("Driver data saved");
        };
    }

    @Bean
    Consumer<Message<VehicleMessage>> vehicleInputSsl(VehicleProvider vehicleProvider, VehicleMapper vehicleMapper) {
        return rawMessage -> {
            log.debug("New vehicle data received...");
            var message = rawMessage.getPayload();
            log.debug("Saving vehicle data...");
            vehicleProvider.save(vehicleMapper.toModel(message));
            log.debug("Vehicle data saved");
        };
    }

    @Bean
    Consumer<Message<ShiftMessage>> shiftInputSsl(DriverProvider driverProvider, ShiftProcessor shiftProcessor) {
        return rawMessage -> {
            log.debug("New shift data received...");
            if (Source.TRIPS.name().equals(rawMessage.getHeaders().get("source"))) {
                log.debug("Shift data was not saved - current service is the source of message");
                return;
            }
            var message = rawMessage.getPayload();
            var driver = driverProvider.get(message.driverId());
            if (driver.isEmpty()) {
                log.debug("Driver not found. Make sure that the driver on the specified shift has type PASSENGER or BOTH.");
                return;
            }
            shiftProcessor.processShift(message, driver.get());
        };
    }

    @Bean
    Consumer<Message<TripFactDistanceMessage>> factDistanceInputSsl(RequestService requestService) {
        return rawMessage -> {
            var payload = rawMessage.getPayload();
            var tripId = payload.getId();
            var distances = payload.getDistances();
            requestService.updateFinal(tripId, distances);
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
