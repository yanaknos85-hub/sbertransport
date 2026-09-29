package ru.sber.transport.spreadsheet.base;

import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.model.ValidationError;
import ru.sber.transport.spreadsheet.base.reader.exception.SheetValidationFailedException;

import java.util.*;

/**
 * Базовый класс страницы.
 *
 * @param <D> тип данных.
 * @param <SW> внутренний тип рабочей книги.
 * @param <TS> внешний тип рабочей страницы.
 * @param <R> тип строки.
 * @param <C> тип ячейки.
 */
@SuppressWarnings("java:S119")
@Getter
public abstract class BaseSheet<D, SW, TS, R, C> extends BaseColumns<D> {

    /**
     * Рабочая книга.
     */
    @Setter
    private SW workbook;

    /**
     * Name of sheet.
     */
    private String name;

    /**
     * Индекс страницы.
     */
    private Integer index;

    /**
     * Класс элемента страницы.
     */
    private final Class<D> valueClass;

    /**
     * Data of sheet.
     */
    private final List<D> data = new ArrayList<>();

    /**
     * Style of header.
     */
    private final StyleImpl headerStyle;

    /**
     * Количество строк.
     */
    private int rowCount = -1;

    /**
     * Верхний колонтитул
     */
    @Setter
    private List<List<String>> caption;

    /**
     * Создать страницу.
     *
     * @param name наименование страницы.
     * @param index индекс страницы.
     * @param valueClass класс значения.
     * @param headerStyle стиль заголовков.
     */
    protected BaseSheet(String name, Integer index, Class<D> valueClass, StyleImpl headerStyle) {
        this.name = name;
        this.index = index;
        this.valueClass = valueClass;
        this.headerStyle = headerStyle;
    }

    /**
     * Создать страницу.
     *
     * @param name наименование страницы.
     * @param valueClass класс значения.
     * @param headerStyle стиль заголовков.
     */
    protected BaseSheet(String name, Class<D> valueClass, StyleImpl headerStyle) {
        this.name = name;
        this.valueClass = valueClass;
        this.headerStyle = headerStyle;
    }

    /**
     * Clear data.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Add row.
     *
     * @param item item to add row.
     */
    public void addRow(D item) {
        data.add(item);
    }

    /**
     * Set data to sheet.
     *
     * @param data data.
     */
    public void setData(Collection<D> data) {
        clear();
        data.forEach(this::addRow);
    }

    /**
     * Прочитать страницу.
     *
     * @param workSheet страница.
     * @param validator валидатор.
     * @param ignoreFormulas флаг игнорирования формул.
     * @param checkHeadMatch флаг проверки правильноста заголовка.
     */
    public void read(TS workSheet, Validator validator, boolean ignoreFormulas, boolean checkHeadMatch) {
        if (name.matches("Sheet\\d+") && !name.equals(getSheetName(workSheet))) {
            name = getSheetName(workSheet);
        }
        var iterator = dataIterator(workSheet, validator, ignoreFormulas, checkHeadMatch);
        var result = new LinkedList<D>();
        var errors = new LinkedList<ValidationError>();
        while (iterator.hasNext()) {
            try {
                var value = iterator.next();
                if (value != null) {
                    result.add(value);
                }
            } catch (ValidationError e) {
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new SheetValidationFailedException(getSheetName(workSheet), errors);
        }
        setData(result);
    }

    /**
     * Прочитать страницу.
     *
     * @param workSheet страница.
     * @param validator валидатор.
     */
    public void read(TS workSheet, Validator validator) {
        read(workSheet, validator, true, false);
    }

    /**
     * Прочитать страницу.
     *
     * @param workSheet страница.
     * @param ignoreFormulas флаг игнорирования формул.
     */
    public void read(TS workSheet, boolean ignoreFormulas) {
        read(workSheet, null, ignoreFormulas, false);
    }

    /**
     * Прочитать страницу.
     *
     * @param workSheet страница.
     */
    public void read(TS workSheet) {
        read(workSheet, true);
    }

    /**
     * Получить итератор по данным.
     *
     * @param workSheet рабочая страница.
     * @param validator валидатор.
     * @param ignoreFormulas флаг игнорирования формул.
     * @param checkHeadMatch флаг проверки правильноста заголовка.
     * @return итератор.
     */
    public Iterator<D> dataIterator(TS workSheet, Validator validator, boolean ignoreFormulas, boolean checkHeadMatch) {
        var headerReader = createHeaderReader(workSheet);
        var dataReader = createDataReader(workSheet, valueClass, getColumns());

        var headerRows = headerReader.read();
        dataReader.setStartRow(headerRows);
        rowCount = getRowCount(workSheet) - headerRows;
        return dataReader.iterator(ignoreFormulas, validator, checkHeadMatch);
    }

    /**
     * Получить итератор по данным.
     *
     * @param workSheet рабочая страница.
     * @param validator валидатор.
     * @return итератор.
     */
    public Iterator<D> dataIterator(TS workSheet, Validator validator) {
        return dataIterator(workSheet, validator, true, false);
    }

    /**
     * Получить итератор по данным.
     *
     * @param workSheet рабочая страница.
     * @param ignoreFormulas флаг игнорирования формул.
     * @return итератор.
     */
    public Iterator<D> dataIterator(TS workSheet, boolean ignoreFormulas) {
        return dataIterator(workSheet, null, ignoreFormulas, false);
    }

    /**
     * Получить итератор по данным.
     *
     * @param workSheet рабочая страница.
     * @return итератор.
     */
    public Iterator<D> dataIterator(TS workSheet) {
        return dataIterator(workSheet, true);
    }

    /**
     * Получение названия страницы.
     *
     * @param workSheet страница.
     * @return название страницы.
     */
    protected abstract String getSheetName(TS workSheet);

    /**
     * Получение количества строк.
     *
     * @param workSheet страница.
     * @return количество строк.
     */
    protected abstract int getRowCount(TS workSheet);

    /**
     * Создание читателя заголовков.
     *
     * @param workSheet страница.
     * @return чистатель заголовков.
     */
    protected abstract BaseHeaderReader<TS> createHeaderReader(TS workSheet);

    /**
     * Создание читателя данных.
     *
     * @param workSheet страница.
     * @return читатель данных.
     */
    protected abstract BaseDataReader<D, TS, R, C> createDataReader(TS workSheet, Class<D> valueClass, List<Column<D, Object>> columns);
}
