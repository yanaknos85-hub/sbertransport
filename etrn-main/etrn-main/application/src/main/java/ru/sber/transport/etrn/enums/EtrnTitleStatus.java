package ru.sber.transport.etrn.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Статус титула Т3 (уровень title).
 * Определяет жизненный цикл банковского титула ЭТрН от ожидания до принятия оператором.
 */
@Schema(title = "Статус титула ЭТрН", description = "Жизненный цикл банковского титула (Т3)")
public enum EtrnTitleStatus {

    EXPECTED("Т3 ожидается"),
    AVAILABLE("Т3 доступен"),
    WAIT_CONDITIONS("Ожидание выполнения условий"),
    READY_TO_SIGN("Готов к подписанию"),
    SIGNING("В процессе подписания"),
    SIGNED_LOCALLY("Подписан локально"),
    SENT_TO_OPERATOR("Отправлен оператору"),
    ACCEPTED_BY_OPERATOR("Принят оператором"),
    REJECTED("Отклонён");

    private final String description;

    EtrnTitleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}