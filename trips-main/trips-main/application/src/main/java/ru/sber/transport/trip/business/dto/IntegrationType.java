package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тип интеграции.
 * @deprecated Тип интеграции будет удален, когда все контрагенты будут переведены на интеграцию по апи
 */
@Schema(title = "Тип интеграции")
@Deprecated(forRemoval = true)
public enum IntegrationType {

    /**
     * XML.
     */
    @Schema(title = "EMAIL-XML")
    EMAIL_XML_API,

    /**
     * API.
     */
    @Schema(title = "API")
    JSON_API_1_0,

    /**
     * Диспетчерская.
     */
    @Schema(title = "Диспетчерская")
    DISPATCHER,

    /**
     * Интеграция без обновления
     */
    @Schema(title = "Интеграция без обновления")
    EMAIL_XML_WOUR_API,

    /**
     * Без интеграции
     */
    @Schema(title = "Без интеграции")
    NONE,

    /**
     * Диспетчерская по апи
     */
    @Schema(title = "Диспетчерская по апи")
    DISPATCHER_API

}
