package ru.sber.transport.etrn.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Технический статус блокировки (уровень lock).
 * Не является статусом карточки — используется для управления
 * конкурентным доступом к операциям подписания.
 */
@Schema(title = "Статус блокировки", description = "Технический статус управления конкурентным доступом")
public enum LockStatus {

    ACQUIRING("Блокировка захватывается"),
    ACTIVE("Блокировка активна"),
    LOCKED_BY_OTHER("Заблокировано другим пользователем"),
    EXPIRED("Истёк TTL блокировки"),
    RELEASED("Блокировка снята");

    private final String description;

    LockStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}