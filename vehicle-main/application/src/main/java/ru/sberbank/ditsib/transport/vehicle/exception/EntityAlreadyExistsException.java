package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * @author skakun-a
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Entity already exists")
public class EntityAlreadyExistsException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Ресурс %s с id %s уже существует";
    public static final String ENTITY_FOR_CLASS_TEMPLATE = "Ресурс %s уже существует";

    public EntityAlreadyExistsException(String message) {
        super(message);
    }

    public EntityAlreadyExistsException(String fieldValue, UUID id) {
        super(MESSAGE_TEMPLATE.formatted(fieldValue, id));
    }

    public EntityAlreadyExistsException(Class<?> clazz) {
        super(ENTITY_FOR_CLASS_TEMPLATE.formatted(clazz.getSimpleName()));
    }
}
