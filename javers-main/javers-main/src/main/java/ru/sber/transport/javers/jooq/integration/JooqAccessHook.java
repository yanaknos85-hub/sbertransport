package ru.sber.transport.javers.jooq.integration;

import java.util.Optional;

import org.javers.core.graph.ObjectAccessHook;
import org.javers.core.graph.ObjectAccessProxy;
import ru.sber.transport.javers.query.Reflector;

/**
 * Хук для оборачивания объектов.
 *
 * @param <T> тип объекта, прослушиваемый хуком.
 */
public class JooqAccessHook<T> implements ObjectAccessHook<T> {

    @Override
    public Optional<ObjectAccessProxy<T>> createAccessor(T entity) {
        //noinspection unchecked
        return Optional.of(new ObjectAccessProxy<>(() -> entity, (Class<T>) entity.getClass(), Reflector.getId(entity)));
    }
}
