package ru.sber.transport.dispatcher.service;

import lombok.NonNull;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.HasName;
import ru.sber.transport.dispatcher.dto.NewDispatcherDto;
import ru.sber.transport.dispatcher.dto.Projection;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DispatcherSearchDto;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с диспетчерами.
 */
public interface DispatcherService {

    /**
     * Добавление диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcher данные диспетчера.
     * @return данные добавленного диспетчера.
     */
    Dispatcher add(@NonNull UUID contractorId, @NonNull NewDispatcherDto dispatcher);

    /**
     * Изменение диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     * @param dispatcher данные диспетчера.
     * @return отредактированный диспетчер.
     */
    Dispatcher edit(@NonNull UUID contractorId, @NonNull UUID dispatcherId, @NonNull NewDispatcherDto dispatcher);

    /**
     * Удаление диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     */
    void delete(@NonNull UUID contractorId, @NonNull UUID dispatcherId);

    /**
     * Получение диспетчера.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     */
    Optional<Dispatcher> get(@NonNull UUID contractorId, @NonNull UUID dispatcherId);

    /**
     * Получение диспетчера.
     *
     * @param oauthId идентификатор диспетчера во внешней системе.
     * @param contractorId идентификатор контрагента.
     */
    Optional<Dispatcher> findByOauthIdAndContractorId(UUID oauthId, UUID contractorId);

    /**
     * Получение диспетчера.
     *
     * @param dispatcherId идентификатор диспетчера.
     */
    Optional<Dispatcher> get(@NonNull UUID dispatcherId);

    /**
     * Получение диспетчера.
     *
     * @param oauthId идентификатор диспетчера во внешней системе.
     */
    Optional<Dispatcher> getByOauthId(@NonNull UUID oauthId);

    /**
     * Подписание Пдн диспетчером.
     *
     * @param dispatcherId идентификатор диспетчера.
     */
    void signPdn(@NonNull UUID dispatcherId);

    /**
     * Получение диспетчеров.
     *
     * @param contractorId идентификатор контрагента.
     * @param search параметры фильтрации диспетчеров.
     * @param projection проекция.
     * @return список диспетчеров.
     */
    Iterable<HasName> get(@NonNull UUID contractorId, DispatcherSearchDto search, Projection projection);

    /**
     * Удаление диспетчерской контрагента.
     *
     * @param contractorId идентификатор контрагента.
     */
    void delete(UUID contractorId);

    /**
     * Частичное изменение диспетчера.
     *
     * @param id идентификатор диспетчера.
     * @param data данные для частичного изменения.
     */
    void patchDispatcher(UUID id, Map<PatchField, Serializable> data);
}
