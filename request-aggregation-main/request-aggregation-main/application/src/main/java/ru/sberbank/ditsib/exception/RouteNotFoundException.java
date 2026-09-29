package ru.sberbank.ditsib.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class RouteNotFoundException extends BusinessException {

    public RouteNotFoundException() {
        super("Не получен ответ о маршруте");
    }
}
