package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "User is not from the same organization")
public class NotSameOrganizationException extends BusinessException {
    public NotSameOrganizationException(UUID firstOrganization, UUID secondOrganization) {
        super("Пользователи не из одной организации. organizationId1: %s, organizationId2: %s".formatted(firstOrganization, secondOrganization));
    }
}
