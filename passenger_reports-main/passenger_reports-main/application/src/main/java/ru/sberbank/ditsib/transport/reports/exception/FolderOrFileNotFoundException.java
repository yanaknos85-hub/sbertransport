package ru.sberbank.ditsib.transport.reports.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение - В директории файл не найден!
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class FolderOrFileNotFoundException extends IllegalStateException implements HasFileData {

    /**
     * Шаблон сообщения - Файл не найдена.
     */
    public static final String FILE_FORMAT = "File with name '%s' not found into the directory '%s'";

    /**
     * Шаблон сообщения - Директория не найдена.
     */
    public static final String FOLDER_FORMAT = "Directory '%s' not found!";

    /**
     * Название файла
     */
    private final String fileName;

    /**
     * Название директории
     */
    private final String folderName;

    /**
     * Создать исключение.
     *
     * @param pattern шаблон сообщения.
     * @param folder директория.
     */
    public FolderOrFileNotFoundException(String pattern, String folder) {
        super(String.format(pattern, folder));
        folderName = folder;
        fileName = null;
    }

    /**
     * Создать исключение.
     *
     * @param pattern шаблон сообщения.
     * @param fileName имя файла.
     * @param folder директория.
     */
    public FolderOrFileNotFoundException(String pattern, String fileName, String folder) {
        super(String.format(pattern, folder, fileName));
        this.fileName = fileName;
        folderName = folder;
    }
}