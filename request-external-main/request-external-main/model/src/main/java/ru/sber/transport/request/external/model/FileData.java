package ru.sber.transport.request.external.model;

import java.nio.file.Path;

/**
 * Данные файла
 */
public interface FileData {

    /**
     * Возвращает путь к файлу
     *
     * @return путь к файлу
     */
    Path getPath();

    /**
     * Возвращает индекс байта для начала чтения фрагмента
     *
     * @return индекс байта для начала чтения фрагмента
     */
    long getFrom();

    /**
     * Возвращает индекс байта для конца чтения фрагмента
     *
     * @return индекс байта для конца чтения фрагмента
     */
    long getTo();

    /**
     * Возвращает размер файла
     *
     * @return размер файла
     */
    long getSize();

    /**
     * Возвращает название файла
     *
     * @return название файла
     */
    String getFileName();

    /**
     * Возвращает тип контента файла
     *
     * @return тип контента файла
     */
    String getContentType();

}
