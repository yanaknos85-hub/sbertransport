package ru.sberbank.ditsib.geo.providers;

import ru.sberbank.ditsib.geo.dto.AddressDto;
import ru.sberbank.ditsib.geo.model.Address;

import java.util.Collection;

public interface AddressProvider {
    
    /**
     * Получение адреса по координатам.
     *
     * @param address адрес.
     * @return коллекция адресов.
     */
    Collection<AddressDto> getAddress(Address address);
}
