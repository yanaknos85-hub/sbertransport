package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Доступные классы такси.
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Класс такси", description = "Доступные классы такси")
public enum TaxiClass {
    
    /**
     * Эконом.
     */
    ECONOMY("Economy", "Эконом", 0),
    
    /**
     * Комфорт.
     */
    COMFORT("Comfort", "Комфорт", 1),
    
    
    /**
     * Комфорт плюс
     */
    COMFORT_PLUS("Comfort+", "Комфорт+", 2),
    
    /**
     * Бизнес.
     */
    BUSINESS("Business", "Бизнес", 3),

    /**
     * Служебный.
     */
    OFFICIAL("Official", "Служебный", 3),

    /**
     * VIP автобус
     */
    VIP_BUS("Vip bus", "Автобус до 9 мест", 4),


    /**
     * Малый автобус (10-21 мест).
     */
    SMALL_BUS("Small bus", "Автобус от 10 до 21 места", 5),


    /**
     * Средний автобус (22-41 место).
     */
    MIDDLE_BUS("Middle bus", "Автобус от 22 до 41 места", 6),


    /**
     * Большой автобус (42-55 мест).
     */
    LARGE_BUS("Large bus", "Автобус от 42 до 55 места", 7);
    
    /**
     * Поиск класса такси по русскому названию.
     *
     * @param name название.
     * @return класс такси.
     */
    public static Optional<TaxiClass> getByRusName(String name) {
        return Arrays.stream(TaxiClass.values()).filter(value -> value.getRusName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Поиск класса такси по названию.
     *
     * @param name название.
     * @return класс такси.
     */
    public static Optional<TaxiClass> getByName(String name) {
        return Optional.ofNullable(name)
                       .map(String::toUpperCase)
                       .map(TaxiClass::valueOf);
    }
    /**
     * Альтернативное название.
     */
    private final String value;
    
    /**
     * Русское название.
     */
    private final String rusName;
    
    /**
     * Порядок выдачи.
     */
    private final int order;
}
