package ru.sber.transport.trips.cargo.web.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

/**
 * Исключение, выбрасываемое при конфликте данных.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Conflicted request")
@Getter
@RequiredArgsConstructor
public class DataConflictException extends RuntimeException {

    private final Class<?> entity;

    private final ConflictType type;

    private final Serializable changed;

    private final Serializable conflicted;

    private final Serializable conflictedField;

    /**
     * Типы конфликтов.
     */
    public enum ConflictType {
        DRIVER_DISPATCHER_RELATION_CONFLICT,
        VEHICLE_DISPATCHER_RELATION_CONFLICT,
        DRIVER_SHIFT_DATE_CONFLICT,
        VEHICLE_SHIFT_DATE_CONFLICT,

        DRIVER_MUST_BE_ONLINE,

        DRIVER_MUST_NOT_BE_SERVING
    }
}
