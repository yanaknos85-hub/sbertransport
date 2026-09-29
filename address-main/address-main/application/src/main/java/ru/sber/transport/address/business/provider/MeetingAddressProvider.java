package ru.sber.transport.address.business.provider;

import ru.sber.transport.address.business.model.MeetingAddress;

import java.util.List;
import java.util.Optional;

public interface MeetingAddressProvider extends AddressDataProvider<MeetingAddress> {

    Optional<MeetingAddress> get(String label);

    List<MeetingAddress> get();
}
