package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Код закрытия
 */
@Getter
@RequiredArgsConstructor
public enum TaxiTripDecisionCode {

    /**
     * Решено.
     */
    FULLY_RESOLVED(1, "Решено полностью"),

    /**
     * Отказ.
     */
    DENIAL_OF_SERVICE(2, "Отказ в обслуживании"),

    /**
     * Невозможно решить.
     */
    RESOLUTION_NOT_POSSIBLE(3, "Невозможно решить"),

    /**
     * Отменено клиентом.
     */
    CANCELLED_BY_CLIENT(4, "Отозвано клиентом");

    private final int code;

    private final String rusName;
    
    private static final Map<Integer, TaxiTripDecisionCode> codeMap = getCodeMap();

    /**
     * Получение решения из кода.
     *
     * @param code код.
     *
     * @return решение.
     */
    public static TaxiTripDecisionCode fromCode(int code) {
        return codeMap.get(code);
    }

    /**
     * Получение решения из кода.
     *
     * @param code код.
     *
     * @return решение.
     */
    public static TaxiTripDecisionCode fromCode(String code) {
        return codeMap.get(Integer.parseInt(code));
    }
    
    private static Map<Integer, TaxiTripDecisionCode> getCodeMap() {
        Map<Integer, TaxiTripDecisionCode> codeMap = new HashMap<>();
        for (TaxiTripDecisionCode value : values()) {
            codeMap.put(value.code, value);
        }
        return codeMap;
    }
}
