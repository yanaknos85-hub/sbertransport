package ru.sberbank.ditsib.transport.request.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение - Неподдерживаемый формат файла
 */
@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
@Getter
public class UnsupportedFileFormatException extends IllegalArgumentException {

    private static final String MSG_FORMAT = "Unsupported file format: .%s";

    /**
     * Формат файла.
     */
    private final String fileFormat;

    /**
     * Создать новое исключение.
     *
     * @param fileFormat формат файла.
     */
    public UnsupportedFileFormatException(String fileFormat) {
        super(String.format(MSG_FORMAT, fileFormat));
        this.fileFormat = fileFormat;
    }
}