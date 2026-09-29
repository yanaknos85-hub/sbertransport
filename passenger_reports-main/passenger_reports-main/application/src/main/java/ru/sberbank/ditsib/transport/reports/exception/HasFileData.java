package ru.sberbank.ditsib.transport.reports.exception;

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