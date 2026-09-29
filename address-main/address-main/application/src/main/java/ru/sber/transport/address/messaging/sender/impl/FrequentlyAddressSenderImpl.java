package ru.sber.transport.address.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.address.messaging.sender.mapper.FrequentlyAddressMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Implementation of address sender.
 */
@RequiredArgsConstructor
@Component
class FrequentlyAddressSenderImpl implements AddressSender<FrequentlyAddress> {

    @Qualifier("frequentlyAddressOutputAvro")
    private final ObjectProvider<OutputBridge> frequentlyAddressOutputAvro;

    private final FrequentlyAddressMessageMapper mapper;

    @Override
    public void send(FrequentlyAddress address, boolean deleted) {
        var message = mapper.toMessage(address);
        if (deleted) {
            message.setCount(0);
        }
        frequentlyAddressOutputAvro.ifAvailable(ob -> ob.send(message));
    }
}
