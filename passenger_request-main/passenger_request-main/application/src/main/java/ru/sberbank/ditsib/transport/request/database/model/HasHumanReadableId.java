package ru.sberbank.ditsib.transport.request.database.model;

/**
 * Интерфейс для объектов, которые имеют человекочитаемый идентификатор.
 */
public interface HasHumanReadableId {

    /**
     * Получить человекочитаемый идентификатор.
     * @return идентификатор
     */
    String getHumanReadableId();

}
