package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Telemedicine has wrong status for declined")
public class DeclinedTelemedicineException extends RuntimeException {
    
    public static final String MSG_FORMAT = """
                                            Заявка с идентификатором id=%s находится в статусе "%s", и не может быть отменена.
                                            """;
    
    public DeclinedTelemedicineException(UUID medicRequestId, TelemedicineStatus status) {
        super(MSG_FORMAT.formatted(medicRequestId, status.name()));
    }
}
