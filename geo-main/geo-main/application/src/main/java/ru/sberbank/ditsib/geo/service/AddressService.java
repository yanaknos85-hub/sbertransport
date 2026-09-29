package ru.sberbank.ditsib.geo.service;

import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.model.Address;

import java.util.List;

/**
 * Сервис для работы с адресами..
 */
public interface AddressService {
    
    /**
     * Получение адреса по координатам.
     *
     * @param addressRequest params of address request.
     *
     * @return список адресов.
     */
    List<Address> getAddressByCoordinates(AddressRequestDto addressRequest);
    
    /**
     * Получение адресов по тестовому представлению.
     *
     * @param addressRequest params of address request.
     *
     * @return коллекция адресов.
     */
    List<Address> getAddressByLocation(AddressRequestDto addressRequest);
}
