package ru.sber.transport.trips.cargo.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибки обработки полей.
 */
@RequiredArgsConstructor
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class FieldsException extends RuntimeException {

    private final transient Iterable<FieldException> exceptions;

}
