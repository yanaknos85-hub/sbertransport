package ru.sber.transport.dispatcher.validation.model;

import ru.sber.transport.dispatcher.database.model.ConflictReason;

/**
 * Класс результата валидации смены из МАИС
 * @param valid признак успешной проверки
 * @param reason причина неуспешного завершения
 */
public record ValidationShiftFromMaisResult(
        boolean valid,
        ConflictReason reason
) {

    /**
     * Создание объекта класса с признаком успеха
     * @return объект класса
     */
    public static ValidationShiftFromMaisResult success() {
        return new ValidationShiftFromMaisResult(true, null);
    }

    /**
     * Создание объекта класса со статусом ошибки
     * @param reason причина провала проверки
     * @return объект класса
     */
    public static ValidationShiftFromMaisResult fail(ConflictReason reason) {
        return new ValidationShiftFromMaisResult(false, reason);
    }
}
