package ru.sberbank.ditsib.exception.excel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.exception.BusinessException;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File has no name")
public class FileNoNameException extends BusinessException {
    public FileNoNameException() {
        super("Не указано имя файла");
    }
}
