package ru.sber.transport.address.business.use_cases.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.use_cases.mapper.AddressBusinessMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;

@Component
class FavoriteImpl extends BaseAddressImpl<FavoriteAddress> {

    public FavoriteImpl(AddressDataProvider<FavoriteAddress> provider, AddressBusinessMapper<FavoriteAddress> mapper, AddressSender<FavoriteAddress> sender) {
        super(provider, mapper, sender);
    }

    @Override
    protected String uniqueValueName() {
        return "label";
    }

    @Override
    protected String uniqueValue(FavoriteAddress source) {
        return source.getLabel();
    }

    @Override
    protected FavoriteAddress newItem() {
        return new FavoriteAddress();
    }
}
