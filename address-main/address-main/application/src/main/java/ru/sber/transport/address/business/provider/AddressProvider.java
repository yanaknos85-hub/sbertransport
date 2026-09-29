package ru.sber.transport.address.business.provider;

import lombok.NonNull;
import ru.sber.transport.address.business.model.Address;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * Провайдер адресов.
 */
public interface AddressProvider<T extends Address> {

    /**
     * Получение адресов по строке.
     *
     * @param search поисковая строка.
     * @return адреса.
     */
    Set<T> getAddresses(@NonNull String search);

    /**
     * Получение адреса по координатам.
     *
     * @param latitude  широта.
     * @param longitude долгота.
     * @return адрес.
     */
    Optional<T> getAddress(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude);
}
