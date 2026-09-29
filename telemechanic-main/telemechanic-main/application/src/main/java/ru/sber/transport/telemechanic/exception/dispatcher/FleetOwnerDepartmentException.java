package ru.sber.transport.telemechanic.exception.dispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Fleet owner department error")
public class FleetOwnerDepartmentException extends BusinessException {
    
    public static final String NOT_FOUND_MSG = "Не найдено подразделение владельца автопарка %s";
    public static final String NOT_ACTIVE_MSG = "Не активно подразделение владельца автопарка %s";
    public static final String NOT_IN_ORGANIZATION_MSG = "Подразделение %s не найдено в организации с id:%s";
    
    public FleetOwnerDepartmentException(String msg) {
        super(msg);
    }
}
