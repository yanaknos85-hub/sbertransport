package ru.sber.transport.contractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тип интеграции.
 */
@Schema(title = "Тип интеграции")
public enum IntegrationTypeDto {

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
    NONE
}
