package ru.sberbank.ditsib.transport.reports.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение - Неудачная попытка действия с файлом / директорией
 */
@ResponseStatus(HttpStatus.CONFLICT)
@Getter
public class FileActionsFailsException extends IllegalStateException implements HasFileData {

    /**
     * Шаблон сообщения - файл не может быть прочитан.
     */
    public static final String FILE_READ_FORMAT = "File '%s' was not read! Please try again!";

    /**
     * Шаблон сообщения - файл не может быть записан.
     */
    public static final String FILE_WRITE_FORMAT = "File '%s' was not written! Please try again!";

    /**
     * Шаблон сообщения - файл не может быть удален.
     */
    public static final String FILE_DELETE_FORMAT = "File '%s' was not deleted! Please try again!";

    /**
     * Шаблон сообщения - директория не может быть удалена.
     */
    public static final String FOLDER_DELETE_FORMAT = "Folder '%s' was not deleted! Please try again!";

    /**
     * Шаблон сообщения - файл не может быть выгружен.
     */
    public static final String FILE_DOWNLOAD_FORMAT = "File '%s' was not downloaded from folder '%s'! Please try again!";

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
     * @param fileOrFolderName имя файла.
     */
    public FileActionsFailsException(String pattern, String fileOrFolderName) {
        super(String.format(pattern, fileOrFolderName));
        if (pattern.equals(FOLDER_DELETE_FORMAT)) {
            folderName = fileOrFolderName;
            fileName = null;
        } else {
            fileName = fileOrFolderName;
            folderName = null;
        }
    }

    /**
     * Создать исключение.
     *
     * @param pattern шаблон сообщения.
     * @param fileName имя файла.
     * @param folder директория.
     */
    public FileActionsFailsException(String pattern, String fileName, String folder) {
        super(String.format(pattern, fileName, folder));
        this.fileName = fileName;
        folderName = folder;
    }
}