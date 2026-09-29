package ru.sberbank.ditsib.transport.constants.external.taxi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Статус поездки из системы исполнителя
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public enum InboundTaxiTripStatus {

    /**
     * Опубликовано.
     */
    SENT_TO_CONTRACTOR(0, "Опубликовано в системе исполнителя"),

    /**
     * Ожидает назначения.
     */
    WAITING_FOR_ASSIGNMENT(1, "Ожидает назначения"),

    /**
     * Водитель найден.
     */
    DRIVER_ASSIGNED(2, "Закреплен за водителем"),

    /**
     * Подтверждено водителем.
     */
    DRIVER_APPROVED(3, "Заказ подтвержден водителем"),

    /**
     * Водитель выехал.
     */
    DRIVER_ON_THE_WAY(4, "Водитель выехал"),

    /**
     * Водитель ожидает клиента.
     */
    DRIVER_ARRIVED(5, "Водитель ожидает клиента"),

    /**
     * Поездка началась.
     */
    TRIP_IN_PROGRESS(6, "Водитель везет клиента"),

    /**
     * Заказ выполнен.
     */
    ORDER_FINISHED(7, "Заказ выполнен"),

    /**
     * Клиент отменил заказ.
     */
    ORDER_CANCELLED_BY_CLIENT(8, "Заказ отменен клиентом"),

    /**
     * Водитель отменил заказ.
     */
    ORDER_CANCELLED_BY_DRIVER(9, "Заказ отменен водителем"),

    /**
     * Заказ просрочен.
     */
    ORDER_EXPIRED(10, "Заказ просрочен"),

    /**
     * Отправка исполнителю не удалась.
     */
    FAILED_TO_SEND(400, "Не удалось отправить исполнителю"),

    /**
     * Неизвестный статус.
     */
    UNDEFINED(-1, "Не определено");
    
    private final int code;

    private final String rusName;

    /**
     * @return Получение названия.
     */
    public String getName() {
        return toString();
    }

    /**
     * Получение статуса из кода.
     *
     * @param code код статуса.
     *
     * @return статус.
     */
    public static InboundTaxiTripStatus fromCode(String code) {
        try {
            return fromCode(Integer.parseInt(code));
        } catch (Exception e) {
            log.error("InboundTaxiTripStatus code couldn`t be parsed: {}", code);
            return UNDEFINED;
        }
    }
    
    private static final Map<Integer, InboundTaxiTripStatus> codeMap = getCodeMap();

    /**
     * Получение статуса из кода.
     *
     * @param code код статуса.
     *
     * @return статус.
     */
    public static InboundTaxiTripStatus fromCode(int code) {
        return codeMap.get(code);
    }
    
    private static Map<Integer, InboundTaxiTripStatus> getCodeMap() {
        Map<Integer, InboundTaxiTripStatus> codeMap = new HashMap<>();
        for (InboundTaxiTripStatus value : values()) {
            codeMap.put(value.code, value);
        }
        return codeMap;
    }
}
