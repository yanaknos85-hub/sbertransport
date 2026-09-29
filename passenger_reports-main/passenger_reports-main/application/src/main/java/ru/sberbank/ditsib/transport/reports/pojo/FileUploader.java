package ru.sberbank.ditsib.transport.reports.pojo;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileUploader {
    
    /**
     * Загрузить файл на сервер и вернуть путь к нему
     * @param file MultipartFile
     * @return путь к файлу Path
     */
    Path uploadFileAndGetServerPath(MultipartFile file);
    
    /**
     * Удалить папку вместе с содержимым
     * @param folderPath путь к папке Path, где хранятся файлы
     */
    void deleteFolderWithFiles(Path folderPath);
}
