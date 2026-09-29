package ru.sberbank.ditsib.transport.exceptions;

import lombok.Getter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Collection;

/**
 * Неверный тип транспорта.
 */
public class WrongTransportTypeException extends RuntimeException {

    private static final String MSG_FORMAT = "Wrong transport type '%s' in request.";

    /**
     * Список разрешенных типов транспорта.
     */
    @Getter
    private final Collection<TransportTypeEnum> allowedList;

    /**
     * Тип транспорта.
     */
    @Getter
    private final TransportTypeEnum transportType;
    
    /**
     * Создание исключения.
     *
     * @param transportType текущий тип транспорта.
     * @param allowedList разрешенные типы транспорта.
     */
    public WrongTransportTypeException(TransportTypeEnum transportType, Collection<TransportTypeEnum> allowedList) {
        super(String.format(MSG_FORMAT, transportType.getName()));
        this.allowedList = allowedList;
        this.transportType = transportType;
    }
}
