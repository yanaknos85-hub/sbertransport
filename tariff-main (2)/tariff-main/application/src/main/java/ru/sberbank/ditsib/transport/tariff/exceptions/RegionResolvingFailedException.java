package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка, выбрасываемая в случае ошибки при вызове сервиса вычисления региона.
 */
@ResponseStatus(value = HttpStatus.EXPECTATION_FAILED, reason = "Region resolving raised an exception")
public class RegionResolvingFailedException extends RuntimeException {
    
    /**
     * Создать исключение.
     *
     * @param e родительское исключение.
     */
    public RegionResolvingFailedException(Exception e) {
        super("Region resolving raised an exception", e);
    }
    
    /**
     * Создать исключение.
     *
     * @param message сообщение
     */
    public RegionResolvingFailedException(String message) {
        super(message);
    }
}
