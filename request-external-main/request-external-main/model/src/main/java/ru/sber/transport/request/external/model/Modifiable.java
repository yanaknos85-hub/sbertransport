package ru.sber.transport.request.external.model;

import java.time.OffsetDateTime;

/**
 * Интерфейс модифицируемого объекта
 */
public interface Modifiable {

    /**
     * Хэш объекта
     *
     * @return хэш объекта
     */
    String hash();

    /**
     * Дата изменения объекта
     *
     * @return дата изменения объекта
     */
    OffsetDateTime modifiedAt();

}
