package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unknown transport type logic")
public class UnsupportedTransportTypeException extends RuntimeException {
    public static final String MSG_FORMAT = "Логика работы для типа транспорта с ID '%s', не определена";
    
    /**
     * Create a new exception.
     *
     * @param typeEnum ID of transport type.
     */
    public UnsupportedTransportTypeException(TransportTypeEnum typeEnum) {
        super(String.format(MSG_FORMAT, typeEnum.getId()));
    }
}
