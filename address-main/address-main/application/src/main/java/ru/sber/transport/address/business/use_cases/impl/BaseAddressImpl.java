package ru.sber.transport.address.business.use_cases.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.business.model.OwnedAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.business.use_cases.mapper.AddressBusinessMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый класс для бизнес-функций адресов.
 *
 * @param <T> тип адреса.
 */
@RequiredArgsConstructor
abstract class BaseAddressImpl<T extends Address> implements Addresses<T> {

    private final AddressDataProvider<T> provider;

    private final AddressBusinessMapper<T> mapper;

    private final AddressSender<T> sender;

    @Override
    public T save(UUID ownerId, T source) {
        return save(ownerId, UUID.randomUUID(), source);
    }

    @Override
    public T save(UUID ownerId, UUID id, T source) {
        var value = uniqueValue(source);
        if (provider.exists(value, ownerId, id)) {
            throw new DuplicateDataException(source.getClass(), Map.of(uniqueValueName(), value, "owner", ownerId));
        }
        var target = provider.get(ownerId, id).orElseGet(this::newItem);
        mapper.update(target, getSource(source));
        if (target instanceof OwnedAddress ownedAddress) {
            ownedAddress.setOwner(ownerId);
        }
        target = provider.save(target);
        sender.send(target, false);
        return target;
    }

    @Override
    public void delete(UUID ownerId, UUID id) {
        get(ownerId, id).ifPresent(e -> {
            provider.delete(ownerId, id);
            sender.send(e, true);
        });
    }

    @Override
    public Collection<T> get(UUID ownerId) {
        return provider.getOfOwner(ownerId);
    }

    @Override
    public Optional<T> get(UUID ownerId, UUID id) {
        return provider.get(ownerId, id);
    }

    /**
     * Провайдер данных.
     *
     * @return провайдер данных.
     */
    protected AddressDataProvider<T> provider() {
        return provider;
    }

    /**
     * Провайдер данных совместимого класса.
     *
     * @return провайдер данных.
     */
    protected <P extends AddressDataProvider<T>> P provider(Class<P> clazz) {
        return ReflectionUtils.cast(provider(), clazz);
    }

    /**
     * Маппер совместимого класса.
     *
     * @return маппер.
     */
    protected <P extends AddressBusinessMapper<T>> P mapper(Class<P> clazz) {
        return ReflectionUtils.cast(mapper, clazz);
    }

    /**
     * Get source address for filling.
     *
     * @param source source address.
     * @return filled address.
     */
    protected @NonNull T getSource(T source) {
        return source;
    }

    /**
     * Название поля, содержащее уникальные значения.
     *
     * @return название поля.
     */
    protected abstract String uniqueValueName();

    /**
     * Получение значения для проверки уникальности.
     *
     * @return значение.
     */
    protected abstract String uniqueValue(T source);

    /**
     * Получение нового объекта.
     *
     * @return новый объект.
     */
    protected abstract T newItem();

}
