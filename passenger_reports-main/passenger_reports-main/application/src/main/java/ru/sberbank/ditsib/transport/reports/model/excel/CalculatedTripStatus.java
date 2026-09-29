package ru.sberbank.ditsib.transport.reports.model.excel;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус поездки, вычисляемый по параметрам импортированной строки
 */
@Schema(description = "Статус поездки, вычисляемый по параметрам импортированной строки")
public enum CalculatedTripStatus {
    DONE,
    CANCELED,
    ERROR_STRING
}
