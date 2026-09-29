package ru.sber.transport.address.business.use_cases.impl;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.provider.MeetingAddressProvider;
import ru.sber.transport.address.business.use_cases.MeetingAddresses;
import ru.sber.transport.address.business.use_cases.mapper.AddressBusinessMapper;
import ru.sber.transport.address.business.use_cases.mapper.MeetingMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.address.providers.common_address.client.GeoClient;

import java.util.*;

@Component
class MeetingsImpl extends BaseAddressImpl<MeetingAddress> implements MeetingAddresses {

    private final GeoClient geoClient;

    public MeetingsImpl(AddressDataProvider<MeetingAddress> provider, GeoClient geoClient, AddressBusinessMapper<MeetingAddress> mapper, AddressSender<MeetingAddress> sender) {
        super(provider, mapper, sender);
        this.geoClient = geoClient;
    }

    @Override
    protected String uniqueValueName() {
        return "label";
    }

    @Override
    protected String uniqueValue(MeetingAddress source) {
        return source.getLabel();
    }

    @Override
    protected MeetingAddress newItem() {
        return new MeetingAddress();
    }

    @Override
    protected @NonNull MeetingAddress getSource(MeetingAddress source) {
        var search = String.join(", ", source.getCountry(), source.getRegion(), source.getCity(),
            source.getStreet(), source.getHouse(), source.getBuilding());
        var found = geoClient.getAddresses(search, null, null, null, null).stream().findFirst();
        if (found.isPresent()) {
            var target = new MeetingAddress();
            mapper(MeetingMapper.class).update(target, found.get());
            target.setLabel(source.getLabel());
            target.setOrganizationId(source.getOrganizationId());
            return target;
        }
        throw new IllegalStateException("Address for %s not found".formatted(search));
    }

    @Override
    public List<MeetingAddress> get() {
        return provider(MeetingAddressProvider.class).get();
    }
}
