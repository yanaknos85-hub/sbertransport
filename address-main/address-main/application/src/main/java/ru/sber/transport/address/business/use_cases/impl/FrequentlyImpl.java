package ru.sber.transport.address.business.use_cases.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.use_cases.Frequentlies;
import ru.sber.transport.address.business.use_cases.mapper.AddressBusinessMapper;
import ru.sber.transport.address.business.use_cases.mapper.FrequentlyMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;

import java.util.*;

@Component
class FrequentlyImpl extends BaseAddressImpl<FrequentlyAddress> implements Frequentlies {

    public FrequentlyImpl(AddressDataProvider<FrequentlyAddress> provider, AddressBusinessMapper<FrequentlyAddress> mapper, AddressSender<FrequentlyAddress> sender) {
        super(provider, mapper, sender);
    }

    @Override
    protected String uniqueValueName() {
        return null;
    }

    @Override
    protected String uniqueValue(FrequentlyAddress source) {
        return null;
    }

    @Override
    protected FrequentlyAddress newItem() {
        return new FrequentlyAddress();
    }

    @Override
    public void increaseUsage(GeoAddress address, boolean first, UUID employeeId) {
        var target = provider().get(employeeId, address.getId()).orElseGet(() -> mapper(FrequentlyMapper.class).toFrequently(address));
        target.setFirst(first);
        target.setOwner(employeeId);
        target.setCount(target.getCount() + 1);
        save(employeeId, address.getId(), target);
    }
}
