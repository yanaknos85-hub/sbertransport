package ru.sber.transport.request.external.providers.exceptions;

import lombok.Getter;

/**
 * Исключение для ситуации, когда резервирование средств невозможно
 */
@Getter
public class ReserveException extends RuntimeException {

    private final Type type;

    /**
     * Создает исключение для ситуации, когда резервирование средств невозможно
     *
     * @param type тип ошибки
     */
    public ReserveException(Type type) {
        super("Reservation failed: %s".formatted(type.name()));
        this.type = type;
    }

    /**
     * Возможные типы ошибок
     */
    public enum Type {

        /**
         * Недостаточно средств на счету
         */
        NOT_SUFFICIENT,

        /**
         * Неизвестный вид услуги
         */
        SERVICE_NOT_AVAILABLE,

        /**
         * Неизвестный тип услуги
         */
        TYPE_NOT_AVAILABLE,

        /**
         * Данные не найдены
         */
        DATA_NOT_FOUND
    }
}
