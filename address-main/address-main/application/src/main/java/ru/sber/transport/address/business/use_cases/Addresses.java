package ru.sber.transport.address.business.use_cases;

import ru.sber.transport.address.business.model.Address;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Работа с адресами.
 *
 * @param <T> тип адреса.
 */
public interface Addresses<T extends Address> {

    /**
     * Сохранить адрес.
     *
     * @param ownerId идентификатор владельца.
     * @param source данные адреса.
     * @return сохраненный адрес.
     */
    T save(UUID ownerId, T source);

    /**
     * Получение списка адресов, принадлежащих кому-либо.
     *
     * @param ownerId идентификатор владельца.
     * @return список адресов.
     */
    Collection<T> get(UUID ownerId);

    /**
     * Получение списка адреса, принадлежащего кому-либо.
     *
     * @param ownerId идентификатор владельца.
     * @param id идентификатор адреса.
     * @return адрес.
     */
    Optional<T> get(UUID ownerId, UUID id);

    /**
     * Сохранить адрес.
     *
     * @param ownerId идентификатор владельца.
     * @param id идентификатор адреса.
     * @param source данные адреса.
     * @return сохраненный адрес.
     */
    T save(UUID ownerId, UUID id, T source);

    /**
     * Удалить адрес.
     *
     * @param ownerId идентификатор владельца.
     * @param id идентификатор адреса.
     */
    void delete(UUID ownerId, UUID id);

}
