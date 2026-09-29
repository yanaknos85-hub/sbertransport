package ru.sber.transport.trip.messaging.providers;

import ru.sber.transport.trip.business.model.Dispatcher;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Провайдер диспетчеров.
 */
public interface DispatcherProvider {


    /**
     * Сохранение
     * @param dispatcher диспетчер
     */
    int save(Dispatcher dispatcher);

    /**
     * Получение диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     */
    Optional<Dispatcher> get(UUID contractorId, UUID dispatcherId);

    /**
     * Получение диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param oauthId идентификатор диспетчера во внешней системе.
     */
    Optional<Dispatcher> getByContractorIdAndOauthId(UUID contractorId, UUID oauthId);

    /**
     * Получение диспетчера.
     *
     * @param dispatcherId идентификатор диспетчера.
     */
    Optional<Dispatcher> get(UUID dispatcherId);

    /**
     * Получение диспетчера.
     *
     * @param oauthId идентификатор диспетчера во внешней системе.
     */
    Optional<Dispatcher> getByOauthId(UUID oauthId);

    /**
     * Получение списка диспетчеров по ID конрагента и списку ID диспетчеров.
     *
     * @param contractorId идентификатор контрагента.
     * @param ids список ID диспетчеров.
     */
    List<Dispatcher> findAllByContractorIdAndIdIn(UUID contractorId, Set<UUID> ids);

    /**
     * Получение списка диспетчеров по ID диспетчеров.
     * @param ids
     * @returnсписок диспетчеров
     */
    List<Dispatcher> getAllByIds(List<UUID> ids);

}

