package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.dao.CoopTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.SingleTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.mappers.TaxiTripMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class TaxiTripSenderImpl implements TaxiTripSender {

    @Qualifier("taxiTripOutput")
    private final ObjectProvider<OutputBridge> taxiTripOutput;

    private final TaxiTripMapper taxiTripMapper;

    private final CoopTaxiTripRepository coopTaxiTripRepository;

    private final SingleTaxiTripRepository singleTaxiTripRepository;

    @Override
    public void send(TaxiTripMessage taxiTripMessage) {
        log.info("Send message, taxiId:{}, tripId:{}, message:{}", taxiTripMessage.getTaxiId(), taxiTripMessage.getId(), taxiTripMessage);
        taxiTripOutput.ifAvailable(outputBridge -> outputBridge.send(taxiTripMessage));
        log.info("Sent message, taxiId:{}, tripId:{}", taxiTripMessage.getTaxiId(), taxiTripMessage.getId());
    }

    @Override
    public void send(CoopTaxiTrip taxiTrip) {
        final var finalTaxiTrip = coopTaxiTripRepository.getReferenceById(taxiTrip.getId());
        send(taxiTripMapper.toMessage(finalTaxiTrip, false));
    }

    @Override
    public void send(SingleTaxiTrip taxiTrip) {
        final var finalTaxiTrip = singleTaxiTripRepository.getReferenceById(taxiTrip.getId());
        send(taxiTripMapper.toMessage(finalTaxiTrip, false));
    }
}