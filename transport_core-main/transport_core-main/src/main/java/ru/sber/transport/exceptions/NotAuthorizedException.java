package ru.sber.transport.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение, выбрасываемое в случае, если пользователь не авторизован.
 */
@ResponseStatus(code = HttpStatus.FORBIDDEN, value = HttpStatus.FORBIDDEN)
@Getter
public class NotAuthorizedException extends RuntimeException {

    /**
     * Причина исключения
     */
    private final String reason;

    /**
     * Идентификатор сотрудника.
     */
    private final UUID entityId;

    /**
     * Создать исключение.
     *
     * @param employeeId идентификатор пользователя.
     */
    public NotAuthorizedException(UUID employeeId) {
        super("Not authorized");
        this.reason = "Not authorized";
        this.entityId = employeeId;
    }
    
    @Override
    public String getMessage() {
        return String.format("Trying to unauthorized access!!! Employee: %s", entityId);
    }
}