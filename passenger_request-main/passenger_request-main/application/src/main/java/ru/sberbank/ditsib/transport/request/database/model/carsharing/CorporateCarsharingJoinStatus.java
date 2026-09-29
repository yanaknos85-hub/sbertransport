package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус подключения сотрудника к корп.каршерингу
 */
@Schema(title = "Статус подключения сотрудника к корп.каршерингу")
public enum CorporateCarsharingJoinStatus {
    JOINED,
    DECLINED
}
