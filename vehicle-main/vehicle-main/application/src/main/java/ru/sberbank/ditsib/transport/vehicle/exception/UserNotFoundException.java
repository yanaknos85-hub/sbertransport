package ru.sberbank.ditsib.transport.vehicle.exception;

import java.util.UUID;

/**
 * Exception is thrown if user is not found.
 */
public class UserNotFoundException extends NotFoundException {

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
