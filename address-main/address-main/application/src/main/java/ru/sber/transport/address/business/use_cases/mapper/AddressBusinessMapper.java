package ru.sber.transport.address.business.use_cases.mapper;

import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.Address;

/**
 * Interface of business mappers of addresses.
 *
 * @param <T> type of address.
 */
public interface AddressBusinessMapper<T extends Address> {

    /**
     * Update an address.
     *
     * @param target target address to update.
     * @param source source address with data.
     */
    void update(@MappingTarget T target, T source);

}
