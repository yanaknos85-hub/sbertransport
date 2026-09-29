package ru.sberbank.ditsib.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File upload error")
public class FileUploadException extends BusinessException {
    
    public FileUploadException() {
        super("Ошибка при загрузке файла");
    }
}
