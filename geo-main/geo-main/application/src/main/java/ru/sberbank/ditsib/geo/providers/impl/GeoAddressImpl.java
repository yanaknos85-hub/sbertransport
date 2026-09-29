package ru.sberbank.ditsib.geo.providers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo.dto.AddressDto;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.AddressKey;
import ru.sberbank.ditsib.geo.providers.AddressProvider;
import ru.sberbank.ditsib.geo.service.GeoControllerService;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeoAddressImpl implements AddressProvider {
    
    private final GeoControllerService service;
    
    @Override
    public Collection<AddressDto> getAddress(Address address) {
        // собрать строку из объкта адреса, и скормить в сервис, собрать одну строку из адреса
        
        var key =
                AddressKey.builder().latitude(address.getId().getLatitude()).longitude(address.getId().getLongitude()).build();
        var addressParam =
                Address.builder()
                       .country(address.getCountry())
                       .region(address.getRegion())
                       .city(address.getCity())
                       .street(address.getStreet())
                       .house(address.getHouse())
                       .id(key).build();
        
        var location = Stream.of(addressParam.getCountry(), addressParam.getRegion(), addressParam.getCity(),
                  addressParam.getStreet(), addressParam.getHouse()).filter(Objects::nonNull).collect(Collectors.joining(" "));
        
        return service.getAddress(AddressRequestDto.builder().requestType(RequestType.BUILDING).location(location).build());
        
    }
}
