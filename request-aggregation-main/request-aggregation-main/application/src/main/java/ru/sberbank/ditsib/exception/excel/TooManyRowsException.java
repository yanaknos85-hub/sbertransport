package ru.sberbank.ditsib.exception.excel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.exception.BusinessException;

@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE, reason = "Too many rows")
public class TooManyRowsException extends BusinessException {
    public TooManyRowsException(Integer maxRowsCount) {
        super(String.format("Превышено максимальное количество записей в файле. Максимум %s строк", maxRowsCount));
    }
}
