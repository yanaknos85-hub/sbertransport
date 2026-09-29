package ru.sber.transport.trips.cargo.messaging.providers;



import ru.sber.transport.trips.cargo.business.model.IntegrationClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер клиентов интеграции.
 */
public interface IntegrationClientProvider {


    /**
     * Сохранение
     * @param client клиент
     */
    void save(IntegrationClient client);

    /**
     * Получение
     * @param id ид
     * @return клиент
     */
    Optional<IntegrationClient> get(UUID id);
}

