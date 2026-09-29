package ru.sber.transport.trips.cargo.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.message.TripMessage;
import ru.sber.transport.trips.cargo.messaging.mapper.MessageTripMapper;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.senders.TripSender;

@Slf4j
@RequiredArgsConstructor
@Component
class TripSenderImpl implements TripSender {

    @Qualifier("tripOutput")
    private final ObjectProvider<OutputBridge> tripOutput;

    @Qualifier("tripOutputSsl")
    private final ObjectProvider<OutputBridge> tripOutputSsl;

    private final MessageTripMapper tripMapper;

    private final ContractorProvider contractorProvider;

    @Override
    public void send(Trip trip) {
        log.info("Sending trip");
        var message = tripMapper.toMessage(trip);
        message.setContractorDigitId(contractorProvider.getContractorDigitId(trip.getContractorId()));
        message.setType(TripMessage.TripType.CARGO);
        tripOutput.ifAvailable(ob -> ob.send(message));
        tripOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
