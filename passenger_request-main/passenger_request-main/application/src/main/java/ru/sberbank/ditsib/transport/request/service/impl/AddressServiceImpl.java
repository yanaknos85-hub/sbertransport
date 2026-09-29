package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.service.AddressService;

import java.util.Optional;

/**
 * Implementation of service for working with addresses.
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    
    @Override
    public Optional<Address> getByData(Address address) {
        log.debug("Address to find was: latitude: " + address.getLatitude() + "; longitude: " + address.getLongitude());
        var usedAddressOptional = addressRepository.findByAddressCoordinates(address.getLatitude(),
                                                                             address.getLongitude(),
                                                                             PageRequest.of(0, 1));
        
        return usedAddressOptional.get().findAny();
    }

    @Override
    public Address save(Address address) {
        var usedAddress = address.getId() != null
                          ? addressRepository.findById(address.getId()).orElse(Address.builder().build())
                          : Address.builder().build();

        usedAddress = usedAddress.toBuilder()
                                 .building(address.getBuilding())
                                 .city(address.getCity())
                                 .country(address.getCountry())
                                 .house(address.getHouse())
                                 .latitude(address.getLatitude())
                                 .longitude(address.getLongitude())
                                 .region(address.getRegion())
                                 .street(address.getStreet())
                                 .structure(address.getStructure())
                                 .build();
        usedAddress.setExistInVspGosbTbRegistry(false);

        return addressRepository.save(usedAddress);
    }

}
