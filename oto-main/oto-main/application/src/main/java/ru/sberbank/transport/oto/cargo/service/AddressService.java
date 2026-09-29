package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Address;

public interface AddressService {
    Address saveIfNotExists(Address address);
}
