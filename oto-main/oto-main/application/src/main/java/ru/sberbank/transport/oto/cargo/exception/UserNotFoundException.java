package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception is thrown if user is not found.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "User not found")
public class UserNotFoundException extends RuntimeException {
    
    public static final String MSG_FORMAT = "User with ID '%s' not found";
    
    /**
     * Create a new exception.
     *
     * @param userId ID of requested user.
     */
    public UserNotFoundException(UUID userId) {
        super(String.format(MSG_FORMAT, userId));
    }
}