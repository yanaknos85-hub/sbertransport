package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Типы интеграции.
 */
@Getter
@RequiredArgsConstructor
public enum TaxiExternalIntegrationType {

    /**
     * XML без запросов обновления.
     */
    EMAIL_XML_WOUR_API("email интеграция без запросов обновления"),

    /**
     * XML.
     */
    EMAIL_XML_API("email интеграция"),

    /**
     * Диспетчерская
     */
    DISPATCHER("Диспетчерская"),
    /**
     * API - Чернов
     */
    CHERNOV_JSON_API("API ИП Чернов"),

    /**
     * API V1.0
     */
    JSON_API_1_0("Целевое API версия 1.0"),

    /**
     * API GETT
     */
    GETT_API("B2B API GETT"),

    /**
     * API ситимобил
     */
    CITY_API("B2B API CITYMOBIL"),

    /**
     * API яндекс
     */
    YANDEX_API("B2B API YANDEX"),

    /**
     * Отсутствие интеграции
     */
    NONE("NONE");
    
    private final String description;

    /**
     * Описания.
     */
    @UtilityClass
    public static class Constants {

        /**
         * XML без запросов обновления.
         */
        public final String EMAIL_XML_WOUR_API_STRING = "EMAIL_XML_WOUR_API";

        /**
         * XML.
         */
        public final String EMAIL_XML_API_STRING = "EMAIL_XML_API";

        /**
         * API - Чернов
         */
        public final String CHERNOV_JSON_API_STRING = "CHERNOV_JSON_API";

        /**
         * API V1.0
         */
        public final String JSON_API_1_0_STRING = "JSON_API_1_0";

        /**
         * API GETT
         */
        public final String GETT_API = "JSON_API_1_0";

        /**
         * API ситимобил
         */
        public final String CITY_API = "B2B_API_CITYMOBIL";

        /**
         * API яндекс
         */
        public final String YANDEX_API = "B2B_API_YANDEX";

        /**
         * API яндекс
         */
        public final String NONE = "NONE";
    }
}
