package ru.sber.transport.telemechanic.exception.dispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Fleet owner organization error")
public class FleetOwnerOrganizationException extends BusinessException {
    
    public static final String NOT_FOUND_MSG = "Не найдена организация владельца автопарка %s";
    public static final String NOT_ACTIVE_MSG = "Не активна организация владельца автопарка %s";
    public static final String NOT_FLEET_OWNER_MSG = "Организация не является владельцем автопарка. ИД организации %s";
    
    public FleetOwnerOrganizationException(String msg) {
        super(msg);
    }
}
