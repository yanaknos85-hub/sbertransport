package ru.sberbank.ditsib.transport.constants;

import lombok.experimental.UtilityClass;

/**
 * Тип поездки
 */
public enum TripType {

    /**
     * Совместная поездка
     */
    COOP,
    /**
     * Индивидуальная поездка
     */
    SINGLE;

    /**
     * Константы.
     */
    @UtilityClass
    public class Constants {

        /**
         * Совместная поездка
         */
        public final String COOP_STRING = "COOP";

        /**
         * Индивидуальная поездка
         */
        public final String SINGLE_STRING = "SINGLE";
    }
}
