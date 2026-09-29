package ru.sber.transport.spreadsheet.excel;

/**
 * Интерфейс писателя.
 */
public interface Writer {

    /**
     * Записать заголовок.
     *
     * @return количество добавленных строк.
     */
    int write();
    
}
