package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "ewb not found")
public class EwbNotFoundException extends RuntimeException {
    
    public static final String MSG_FORMAT = "ЭПЛ с идентификатором id=%s не найден!";
    public static final String EWB_PATH_MSG_FORMAT = "ЭПЛ с идентификатором сотрудника id=%s на сегодняшний день (today=%s) не найден!";
    public static final String EWB_REQUEST_MSG_FORMAT = "ЭПЛ с идентификатором заявки requestId=%s не найден!";
    public static final String EWB_ON_THE_LINE_FOR_USER_ID_MSG_FORMAT = "Не найден ЭПЛ в статусе На линии на сотрудника ИД:%S";
    
    public EwbNotFoundException(UUID ewbId) {
        super(MSG_FORMAT.formatted(ewbId));
    }
    
    public EwbNotFoundException(UUID driverId, LocalDate today) {
        super(EWB_PATH_MSG_FORMAT.formatted(driverId, today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }
    
    public EwbNotFoundException(UUID id, String msgFormat) {
        super(msgFormat.formatted(id));
    }
}
