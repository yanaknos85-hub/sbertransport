package ru.sberbank.ditsib.transport.constants.external.taxi;

import lombok.experimental.UtilityClass;

/**
 * Исходящие статусы заявок.
 */
public enum OutboundRequestStatus {

    /**
     * Новая заявка на исполнение
     */
    NEW,

    /**
     * Отмененная заявка
     */
    REJECT,

    /**
     * Заявка в процессе исполнения(требуется для запросов обновлнной информации по поездке)
     */
    IN_PROGRESS;

    /**
     * Список констант статусов.
     */
    @UtilityClass
    public static class Constants{

        /**
         * Новая заявка.
         */
        public final String NEW_STRING = "NEW";

        /**
         * Отмененная заявка.
         */
        public final String REJECT_STRING = "REJECT";

        /**
         * Заявка в работе.
         */
        public final String IN_PROGRESS_STRING = "IN_PROGRESS";
    }
}
