package ru.sber.transport.telemechanic.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Organization not allowed")
public class OrganizationNotAllowedException extends RuntimeException {
    
    public static final String MSG_FORMAT = "Информация по организации недоступна";
    
    public OrganizationNotAllowedException() {
        super(MSG_FORMAT);
    }
}
