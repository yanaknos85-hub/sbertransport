package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
public class UserNotFoundException extends BusinessException {

    public static final String MSG_FORMAT = "Пользователь не найден, id:'%s'";

    /**
     * Create a new exception.
     *
     * @param userId ID of requested user.
     */
    public UserNotFoundException(UUID userId) {
        super(String.format(MSG_FORMAT, userId));
    }
}
