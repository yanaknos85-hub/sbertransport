package ru.sberbank.ditsib.exception.excel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sberbank.ditsib.exception.BusinessException;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File upload error")
public class FileUnsupportedExtensionException extends BusinessException {
    
    public static final String FILE_UPLOAD_ERROR = "Ожидается файл формата %s";
    
    public FileUnsupportedExtensionException(String supportedFormats) {
        super(String.format(FILE_UPLOAD_ERROR, supportedFormats));
    }
    
    
}
