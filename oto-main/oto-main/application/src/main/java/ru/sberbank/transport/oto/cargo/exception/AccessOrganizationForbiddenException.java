package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение если поиск по запрашиваемой оргпнизации запрещен
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The search for the requested organization is forbidden")
public class AccessOrganizationForbiddenException extends RuntimeException {
    
    public static final String MSG_FORMAT = "The search for requested organization id= %s is forbidden";
    
    /**
     * Create a new exception.
     *
     * @param organizationId ID of requested organization.
     */
    public AccessOrganizationForbiddenException(UUID organizationId) {
        super(String.format(MSG_FORMAT, organizationId));
    }
}