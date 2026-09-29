package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Ошибка валидации запроса")
public class MultipointLimitExceededException extends BusinessException {

    public MultipointLimitExceededException() {
        super("Превышен лимит поездок с количеством точек > 2");
    }

}
