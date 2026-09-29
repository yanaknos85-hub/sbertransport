package ru.sber.transport.address.business.provider;

import ru.sber.transport.address.business.model.Address;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных адресов.
 */
public interface AddressDataProvider<T extends Address> {

    Collection<T> getOfOwner(UUID userId);

    Optional<T> get(UUID userId, UUID id);

    T save(T address);

    void delete(UUID userId, UUID id);

    boolean exists(String label, UUID userId, UUID... exclusions);
}
