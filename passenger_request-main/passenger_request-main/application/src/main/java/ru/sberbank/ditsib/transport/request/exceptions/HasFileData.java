package ru.sberbank.ditsib.transport.request.exceptions;

/**
 * Объект с данными о фалах.
 */
public interface HasFileData {

    /**
     * @return имя файла.
     */
    String getFileName();

    /**
     * @return имя директории.
     */
    String getFolderName();
}