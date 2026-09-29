package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Доступные классы каршеринга.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Класс каршеринга", description = "Доступные классы каршеринга")
public enum CarsharingClass {
    
    /**
     * Эконом.
     */
    ECONOMY("Economy"),
    
    /**
     * Комфорт.
     */
    COMFORT("Comfort"),

    /**
     * Бизнес.
     */
    BUSINESS("Business");

    private final String value;
}
