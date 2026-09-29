package ru.sber.transport.address.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.address.messaging.sender.mapper.FavoriteAddressMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Implementation of address sender.
 */
@RequiredArgsConstructor
@Component
class FavoriteAddressSenderImpl implements AddressSender<FavoriteAddress> {

    @Qualifier("favoriteAddressOutputAvro")
    private final ObjectProvider<OutputBridge> favoriteAddressOutputAvro;

    private final FavoriteAddressMessageMapper mapper;

    @Override
    public void send(FavoriteAddress address, boolean deleted) {
        var message = mapper.toMessage(address, deleted);
        favoriteAddressOutputAvro.ifAvailable(ob -> ob.send(message));
    }

}
