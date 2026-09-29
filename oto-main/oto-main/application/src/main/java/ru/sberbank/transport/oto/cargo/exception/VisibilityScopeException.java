package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception of visibility scope
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Visibility scope exception")
public class VisibilityScopeException extends RuntimeException {

    public static final String ERROR_MESSAGE = "Не указаны данные для поиска по организации или группе исполнителей";
    public static final String ERROR_MESSAGE_BOTH_SCOPE = "Поиск по организации и группе исполнителей не может быть выполнен одновременно";

    public VisibilityScopeException(String s) {
        super(s);
    }
}