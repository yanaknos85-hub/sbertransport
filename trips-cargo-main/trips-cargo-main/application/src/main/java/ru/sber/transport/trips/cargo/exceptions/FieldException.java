package ru.sber.transport.trips.cargo.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Ошибка работы с полем.
 */
@RequiredArgsConstructor
@Getter
public class FieldException extends RuntimeException {

    private final String field;

    private final Serializable value;

    private final Class<?> requiredType;

}
