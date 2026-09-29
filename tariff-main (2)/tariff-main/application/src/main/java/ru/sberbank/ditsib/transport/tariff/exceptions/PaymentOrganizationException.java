package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при нарушении логики работы организациями ответственными за оплату
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Contract payment oranization problem")
public class PaymentOrganizationException extends RuntimeException {
    /**
     * Новое исключение.
     * @param message message.
     */
    public PaymentOrganizationException(String message) {
        super(message);
    }
}
