package ru.sber.transport.address.providers.model;

import org.jooq.Record;

import java.util.UUID;

/**
 * Interface for entities with an ID field.
 */
public interface HasId extends Record {

    /**
     * Set value of an ID field.
     *
     * @param id value.
     */
    void setId(UUID id);

    /**
     * Get value of an ID field.
     *
     * @return value.
     */
    UUID getId();

}
