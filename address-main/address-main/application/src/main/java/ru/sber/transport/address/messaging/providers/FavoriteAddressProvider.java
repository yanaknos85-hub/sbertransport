package ru.sber.transport.address.messaging.providers;

import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер работы с любимыми адресами.
 */
public interface FavoriteAddressProvider extends AddressDataProvider<FavoriteAddress> {
    
    /**
     * Удаление.
     *
     * @param id идентификатор адреса на удаление.
     */
    void delete(UUID id);
    
    /**
     * Получение частого адреса.
     *
     * @param id идентификатор адреса.
     * @return адрес.
     */
    Optional<FavoriteAddress> get(UUID id);
}
