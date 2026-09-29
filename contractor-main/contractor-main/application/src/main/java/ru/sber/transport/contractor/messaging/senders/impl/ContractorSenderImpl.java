package ru.sber.transport.contractor.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.messaging.senders.ContractorSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Implementation of contractor sender.
 */
@RequiredArgsConstructor
@Component
class ContractorSenderImpl implements ContractorSender {

    @Qualifier("contractorOutput")
    private final ObjectProvider<OutputBridge> contractorOutput;

    @Qualifier("contractorOutputSsl")
    private final ObjectProvider<OutputBridge> contractorOutputSsl;

    private final ContractorMapper mapper;

    @Override
    public void send(Contractor contractor) {
        contractorOutput.ifAvailable(ob -> ob.send(mapper.toMessage(contractor)));
        contractorOutputSsl.ifAvailable(ob -> ob.send(mapper.toMessage(contractor)));
    }
}
