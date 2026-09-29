package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ForbiddenOrganizationException extends BusinessException {
    
    public static final String MESSAGE = "Организация пользователя не совпадает с организацией создателя ЭПЛ. ИД организации пользователя: %s";
    
    public ForbiddenOrganizationException(UUID organizationId) {
        super(MESSAGE.formatted(organizationId));
    }
}
