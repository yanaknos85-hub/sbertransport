package ru.sber.transport.address.providers.indexed_address.mapper;

import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.providers.model.IndexedRecord;

/**
 * Interface for database address mappers.
 *
 * @param <T> type of entity.
 * @param <R> type of record.
 */
public interface AddressMapperDatabase<T extends Address, R extends IndexedRecord> {

    /**
     * Convert database value to business.
     *
     * @param source database value.
     * @return business value.
     */
    T toBusiness(R source);

    /**
     * Convert business value to database.
     *
     * @param source business value.
     * @return database value.
     */
    R toModel(T source);

    /**
     * Update target with data from source.
     *
     * @param target target to update.
     * @param source source.
     */
    void update(@MappingTarget R target, T source);
}
