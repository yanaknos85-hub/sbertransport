package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.AddressRepository;
import ru.sberbank.ditsib.transport.reports.model.Address;
import ru.sberbank.ditsib.transport.reports.service.AddressService;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    @Override
    public Address saveIfNotExists(Address address) {
        return addressRepository.findById(address.getId()).orElseGet(
                () -> addressRepository.save(address)
        );
    }
}
