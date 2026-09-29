package ru.sber.transport.etrn.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус операции подписания УКЭП (уровень signing).
 * Определяет жизненный цикл операции подписания от создания до подтверждения КОРУС.
 */
@Schema(title = "Статус операции подписания", description = "Жизненный цикл УКЭП-подписания ЭТрН")
public enum SigningOperationStatus {

    CREATED("Операция создана"),
    SIGNING("Идёт процесс подписания"),
    SIGNED_LOCALLY("Подписано локально"),
    SENDING_TO_KORUS("Отправляется в КОРУС"),
    ACCEPTED_BY_KORUS("Подтверждено КОРУС"),
    REJECTED_BY_KORUS("Отклонено КОРУС"),
    OUTCOME_UNKNOWN("Исход неизвестен"),
    ERROR("Ошибка операции");

    private final String description;

    SigningOperationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}