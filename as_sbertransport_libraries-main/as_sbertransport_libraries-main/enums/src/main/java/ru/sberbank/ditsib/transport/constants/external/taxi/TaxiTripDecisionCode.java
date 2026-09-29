package ru.sberbank.ditsib.transport.constants.external.taxi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Коды решений.
 */
@Getter
@RequiredArgsConstructor
public enum TaxiTripDecisionCode {

    /**
     * Решено.
     */
    RESOLVED(1, "Решено полностью"),

    /**
     * Отказ.
     */
    DENIAL_OF_SERVICE(2,"Отказ в обслуживании"),

    /**
     * Решить невозможно.
     */
    RESOLUTION_IMPOSSIBLE(3,"Невозможно решить"),

    /**
     * Отозвано.
     */
    CALLED_OFF_BY_CLIENT(4,"Отозвано клиентом"),

    /**
     * Не определено.
     */
    UNDEFINED(-1,"Не определено");

    private final int code;

    private final String rusName;
    
    private static final Map<Integer, TaxiTripDecisionCode> codeMap = getCodeMap();

    /**
     * Получение решения по коду.
     *
     * @param code код.
     *
     * @return решение.
     */
    public static TaxiTripDecisionCode fromCode(String code) {
        try {
            return fromCode(Integer.parseInt(code));
        } catch (Exception e){
            return UNDEFINED;
        }
    }

    /**
     * Получение решения по коду.
     *
     * @param code код.
     *
     * @return решение.
     */
    public static TaxiTripDecisionCode fromCode(int code) {
        return codeMap.get(code);
    }
    
    private static Map<Integer, TaxiTripDecisionCode> getCodeMap() {
        Map<Integer, TaxiTripDecisionCode> codeMap = new HashMap<>();
        for (TaxiTripDecisionCode value : values()) {
            codeMap.put(value.code, value);
        }
        return codeMap;
    }
}
