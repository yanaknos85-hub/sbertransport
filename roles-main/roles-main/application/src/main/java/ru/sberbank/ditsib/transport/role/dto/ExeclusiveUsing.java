package ru.sberbank.ditsib.transport.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Exclusive role for.
 */
@Schema(title = "Признак эксклюзивного использования роли")
public enum ExeclusiveUsing {

    /**
     * Только внутренний пользователь
     */
    INTERNAL,

    /**
     * Только внешний пользователь
     */
    EXTERNAL
}
