package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Desired date is not in time range")
public class CarLocationDesiredDateNotInTimeRangeException extends BusinessException {

    private static final String MSG = """
            Дата и время подачи такси для заявки requestId=%s не входит в диапазон времени - за 60 минут до подачи, и 30 минут после.
            Дата и время подачи такси: %s. Текущее время: %s.
            """;

    public CarLocationDesiredDateNotInTimeRangeException(UUID requestId, LocalDateTime desiredDate, LocalDateTime currentDateTime) {
        super(MSG.formatted(requestId, desiredDate, currentDateTime));
    }
}
