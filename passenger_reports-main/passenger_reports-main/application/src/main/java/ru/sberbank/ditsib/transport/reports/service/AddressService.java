package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Address;

public interface AddressService {
    Address saveIfNotExists(Address address);
}
