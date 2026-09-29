package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.AddressRepository;
import ru.sberbank.transport.oto.cargo.database.model.Address;
import ru.sberbank.transport.oto.cargo.service.AddressService;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    @Override
    public Address saveIfNotExists(Address address) {
        if (address.getId() == null || addressRepository.findById(address.getId()).isEmpty()) {
            address.setId(UUID.randomUUID());
            return addressRepository.save(address);
        } else {
            return addressRepository.findById(address.getId()).get();
        }
    }
}
