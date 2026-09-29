package ru.sberbank.ditsib.geo.service;

import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.model.Address;

import java.util.List;
import java.util.function.Supplier;

/**
 * Сервис кеширования.
 */
public interface CacheService {

    /**
     * Получение адреса с учетом кеширования.
     *
     * @param addressRequest      запрос.
     * @param actualValueSupplier функция получения данных от 2гис.
     * @return список адресов.
     */
    List<Address> getAddressByLocation(AddressRequestDto addressRequest, Supplier<List<Address>> actualValueSupplier);
}
