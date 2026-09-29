package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при неверном значении НДС
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Not valid vat value")
public class VatValueException extends RuntimeException {
    /**
     * Исключение при неверном значении НДС
     * @param message message.
     */
    public VatValueException(String message) {
        super(message);
    }
}
