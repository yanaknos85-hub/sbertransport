package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Incomplete list of safety checks")
public class IncompleteListOfChecksException extends BusinessException{
    
    public IncompleteListOfChecksException() {
        super("Получен не полный список проверок безопасности.");
    }
}
