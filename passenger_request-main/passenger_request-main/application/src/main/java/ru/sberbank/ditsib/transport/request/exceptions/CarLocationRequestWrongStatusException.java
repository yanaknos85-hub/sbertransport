package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Wrong request for taxi status")
public class CarLocationRequestWrongStatusException extends BusinessException {

    private static final String MSG = "Невозможно получить местоположение автомобиля. Заявка requestId=%s в статусе %s";

    public CarLocationRequestWrongStatusException(UUID requestId, TripRequestStatus status) {
        super(MSG.formatted(requestId, status.getDescription()));
    }
}
