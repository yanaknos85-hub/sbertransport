package ru.sberbank.ditsib.transport.request.service;

import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.request.database.model.Address;

import java.util.Optional;

/**
 * Service for working with addresses.
 */
public interface AddressService {
    
    /**
     * Get address by address data.
     *
     * @param address source address.
     *
     * @return address.
     */
    Optional<Address> getByData(@NotNull Address address);
    
    /**
     * Save address.
     *
     * @param address address.
     *
     * @return address.
     */
    Address save(@NotNull Address address);

}
