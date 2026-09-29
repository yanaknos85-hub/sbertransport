package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.dto.FileData;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    
    /**
     * Загрузка файла
     *
     * @param file файл для загрузки
     * @param fileName наименование файла
     * @param contentType тип файла
     */
    void upload(InputStream file, String fileName, String contentType) throws IOException;
    
    /**
     * Получение файла.
     *
     * @param fileName наименование файла
     *
     * @return дто с массивом данных файла
     *
     * @throws IOException чтение файла завершилось с ошибкой.
     */
    FileData get(String fileName) throws IOException;
    
    /**
     * Удаление файла.
     *
     * @param fileName наименование файла.
     */
    void delete(String fileName);
}
