package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.transport.request.database.model.Request;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Cancellation time for the request for the ride has expired")
public class CancellationTimeException extends RuntimeException {
    public static final String MSG_FORMAT = "Истекло время отмены заявки '%s'";
    
    public CancellationTimeException(Request request) {
        super(MSG_FORMAT.formatted(request.getHumanReadableId()));
    }
    
}
