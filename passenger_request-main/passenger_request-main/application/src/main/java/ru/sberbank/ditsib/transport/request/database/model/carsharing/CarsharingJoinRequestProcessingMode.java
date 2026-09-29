package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Режим обработки заявок на подключение к корп.каршерингу
 */
@Schema(title = "Режим обработки заявок на подключение к корп.каршерингу")
public enum CarsharingJoinRequestProcessingMode {
    MANUALLY_BY_ENGINEER,
    AUTOMATICALLY_BY_INTEGRATION
}
