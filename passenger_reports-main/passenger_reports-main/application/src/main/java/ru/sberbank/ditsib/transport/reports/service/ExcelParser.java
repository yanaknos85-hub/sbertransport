package ru.sberbank.ditsib.transport.reports.service;

import java.util.List;
import java.util.Set;

/**
 * Парсер таблиц Excel
 *
 * @param <T> тип данных.
 */
public interface ExcelParser<T> {
    
    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>без проверки шапки</b>
     *
     * @param filePath абсолютный путь у существующему файлу в системе
     * @param headerStringsQnt Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @return список импортированных строк
     */
    List<T> parsingExcelTableWithoutHeaderCheck(
            String filePath,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt);

    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>без проверки шапки</b>
     * @param fileBytes byte[] Массив байтов представляющих XLS/XLSX документ
     * @param headerStringsQnt Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @return список импортированных строк
     */
    List<T> parsingExcelTableWithoutHeaderCheck(
            byte[] fileBytes,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt);


    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>с проверкой шапки</b>
     * @param filePath абсолютный путь у существующему файлу в системе
     * @param headerStringsQnt Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @param columnNamesRowNum Номер строки, содержащей заголовки столбцов
     * @param columnNames требуемые заголовки столбцов
     * @return список импортированных строк
     */
    List<T> parsingExcelTableWithHeaderCheck(
            String filePath,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt,
            int columnNamesRowNum, List<String> columnNames);



    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>с проверкой шапки</b>
     * @param fileBytes byte[] Массив байтов представляющих XLS/XLSX документ
     * @param headerStringsQnt Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @param columnNamesRowNum Номер строки, содержащей заголовки столбцов
     * @param columnNames требуемые заголовки столбцов
     * @return список импортированных строк
     */
    List<T> parsingExcelTableWithHeaderCheck(
            byte[] fileBytes,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt,
            int columnNamesRowNum, List<String> columnNames);
    
    /**
     * Геттер, возвращающий множество адресов ячеек, содержащих некорректные данные
     *
     * @return адреса некорректных ячеек.
     */
    Set<String> getIncorrectTypeCells();
    
    /**
     * Геттер, возвращающий множество адресов ячеек, которые являются пустыми.
     *
     * @return адреса пустых ячеек.
     */
    Set<String> getBlankCells();
}
