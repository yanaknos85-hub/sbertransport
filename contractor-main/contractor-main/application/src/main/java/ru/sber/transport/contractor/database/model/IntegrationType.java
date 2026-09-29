package ru.sber.transport.contractor.database.model;

/**
 * Тип интеграции.
 */
public enum IntegrationType {

    /**
     * XML.
     */
    EMAIL_XML_API,

    /**
     * API.
     */
    JSON_API_1_0,

    /**
     * Диспетчерская
     */
    DISPATCHER,

    /**
     * Интеграция без обновления
     */
    EMAIL_XML_WOUR_API,

    /**
     * Интеграция без обновления
     */
    NONE
}
