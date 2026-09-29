package ru.sberbank.ditsib.transport.tariff.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.mappers.ContractMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.ContractSender;

/**
 * Implementation of sender contracts.
 */
@RequiredArgsConstructor
@Component
class ContractSenderImpl implements ContractSender {

    @Qualifier("contractOutput")
    private final ObjectProvider<OutputBridge> contractOutput;

    private final ContractMapper mapper;

    @Override
    public void send(Contract entity, boolean deleted) {
        var contractMessage = mapper.toMessage(entity, deleted);
        contractOutput.ifAvailable(it -> it.send(contractMessage));
    }
}
