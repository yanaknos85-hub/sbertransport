package ru.sberbank.ditsib.transport.reports.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение - Файл не был передан
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class IllegalFileException extends IllegalArgumentException implements HasFileData {

    /**
     * Файл не передан
     */
    public static final String NOT_TRANSFERRED_MSG = "File was not transferred!";

    /**
     * Неверное имя файла.
     */
    public static final String FILE_NAME_FORMAT = "Incorrect file name: '%s'";

    /**
     * Неверное имя директории.
     */
    public static final String FOLDER_NAME_FORMAT = "Incorrect folder name: '%s'";

    /**
     * Имя файла.
     */
    private final String fileName;

    /**
     * Имя директории.
     */
    private final String folderName;

    /**
     * Создать новое исключение.
     *
     * @param message сообщение исключения.
     */
    public IllegalFileException(String message) {
        super(message);
        folderName = null;
        fileName = null;
    }

    /**
     * Новое исключение.
     *
     * @param pattern шаблон сообщения.
     * @param fileOrFolderName имя файла.
     */
    public IllegalFileException(String pattern, String fileOrFolderName) {
        super(String.format(pattern, fileOrFolderName));
        if (pattern.equals(FILE_NAME_FORMAT)) {
            fileName = fileOrFolderName;
            folderName = null;
        } else {
            folderName = fileOrFolderName;
            fileName = null;
        }
    }
}