package ru.sberbank.ditsib.transport.request.exceptions;

import jakarta.persistence.NonUniqueResultException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws if address with label already added.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Address duplicates the previous one")
public class AddressDuplicateOneByOneException extends NonUniqueResultException {
    
    public static final String MSG_FORMAT = "The address by position %d duplicates the previous one";
    
    /**
     * Create a new exception.
     */
    public AddressDuplicateOneByOneException(int position) {
        super(String.format(MSG_FORMAT, position));
    }
}
