package ru.sberbank.ditsib.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Некорректное время заказа")
public class LeadDepartureTimeException extends BusinessException {

    private static final String MESSAGE =
            "Дата и время заказа должны быть с учетом задержки в %s часа от текущего времени";

    public LeadDepartureTimeException(int departureLag) {
        super(String.format(MESSAGE, departureLag));
    }
}
