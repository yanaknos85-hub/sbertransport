package ru.sberbank.ditsib.exception.excel;

import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.exception.BusinessException;

@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE, reason = "Excel file too large")
public class MaxFileSizeException extends BusinessException {
    public MaxFileSizeException(DataSize maxSize) {
        super("Превышен максимальный размер файла " + maxSize);
    }
}
