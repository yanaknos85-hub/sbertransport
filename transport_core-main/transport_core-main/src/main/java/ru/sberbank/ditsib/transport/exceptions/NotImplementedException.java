package ru.sberbank.ditsib.transport.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое в случае, если запрашиваемая операция не реализована.
 */
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED, reason = "Method not implemented")
public class NotImplementedException extends RuntimeException {
}
