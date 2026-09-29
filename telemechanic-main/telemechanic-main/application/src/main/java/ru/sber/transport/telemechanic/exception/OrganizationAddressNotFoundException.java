package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Organization address not found")
public class OrganizationAddressNotFoundException extends BusinessException {
    public OrganizationAddressNotFoundException(UUID organizationId) {
        super("Адрес организации не найден! organizationId=%s".formatted(organizationId));
    }
}
