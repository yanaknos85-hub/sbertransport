package ru.sber.transport.etrn.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус карточки ЭТрН (уровень card).
 * Определяет жизненный цикл карточки от создания до завершения процесса.
 * Допустимая цепочка: IDENTIFIED → WAIT_KORUS_DATA → WAIT_CONDITIONS / READY_FOR_BANK_ACTION
 * → WAIT_KORUS_CONFIRMATION → PROCESS_COMPLETED
 */
@Schema(title = "Статус карточки ЭТрН", description = "Жизненный цикл карточки электронной транспортной накладной")
public enum EtrnCardStatus {

    IDENTIFIED("Карточка создана, идентичность разрешена"),
    WAIT_KORUS_DATA("Ожидание входящих данных от КОРУС"),
    WAIT_CONDITIONS("Ожидание выполнения условий"),
    READY_FOR_BANK_ACTION("Готовность к действию Банка"),
    WAIT_KORUS_CONFIRMATION("Ожидание подтверждения от КОРУС"),
    PROCESS_COMPLETED("Процесс завершён");

    private final String description;

    EtrnCardStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}