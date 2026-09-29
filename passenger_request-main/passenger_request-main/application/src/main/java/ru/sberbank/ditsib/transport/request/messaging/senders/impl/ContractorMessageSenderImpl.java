package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.ContractorMessageSender;

/**
 * Реализация отправителя.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class ContractorMessageSenderImpl implements ContractorMessageSender {

    @Qualifier("contractorTaxiTripOutput")
    private final ObjectProvider<OutputBridge> contractorTaxiTripOutput;

    @Override
    public void send(OutContractorTaxiTripMessage message) {
        if (message != null) {
            log.info("Send message, taxiId:{}, tripId:{}, message:{}", message.taxiId(), message.tripId(), message);
            contractorTaxiTripOutput.ifAvailable(it -> it.send(message));
            log.info("Sent message, taxiId:{}, tripId:{}", message.taxiId(), message.tripId());
        } else {
            log.info("Attempting to send null-message, for example, if a request has no link to a contractor");
        }
    }
}
