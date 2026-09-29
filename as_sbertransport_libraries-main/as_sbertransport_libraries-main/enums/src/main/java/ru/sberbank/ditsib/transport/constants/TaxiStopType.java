package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;


/**
 * Enum Тип точки маршрута
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaxiStopType {

    /**
     * Посадка.
     */
    BOARDING(TaxiStopType.BOARDING_RUS_NAME),

    /**
     * Высадка.
     */
    UNBOARDING(TaxiStopType.UNBOARDING_RUS_NAME),

    /**
     * Ожидание.
     */
    WAIT(TaxiStopType.WAIT_RUS_NAME);


    /**
     * Посадка.
     */
    public static final String BOARDING_RUS_NAME = "Посадка";

    /**
     * Высадка.
     */
    public static final String UNBOARDING_RUS_NAME = "Высадка";

    /**
     * Ожидание.
     */
    public static final String WAIT_RUS_NAME = "Ожидание";
    
    
    private final String rusName;
    
    private static final Map<String, TaxiStopType> rusNameMap = Map.of(BOARDING_RUS_NAME, BOARDING,
                                                                       UNBOARDING_RUS_NAME, UNBOARDING,
                                                                       WAIT_RUS_NAME, WAIT);

    /**
     * Получение типа остановки по русскому названию.
     *
     * @param rusName название.
     * @return тип остановки.
     */
    public static TaxiStopType fromRusName(String rusName) {
        return rusNameMap.get(rusName);
    }
}
