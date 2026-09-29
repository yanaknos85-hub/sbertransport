package ru.sber.transport.spreadsheet.fastexcel;

import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;
import ru.sber.transport.spreadsheet.fastexcel.reader.FastExcelDataReader;
import ru.sber.transport.spreadsheet.fastexcel.reader.FastExcelHeaderReader;

import java.util.List;

/**
 * Рабочая страница для работы с FastExcel.
 *
 * @param <T>
 */
public class FastExcelSheet<T> extends BaseSheet<T, FastExcel, Sheet, Row, Cell> {

    /**
     * Создать рабочую страницу.
     *
     * @param name       название.
     * @param valueClass класс данных.
     * @param style      стиль.
     */
    public FastExcelSheet(String name, Class<T> valueClass, StyleImpl style) {
        super(name, valueClass, style);
    }

    /**
     * Создать рабочую страницу.
     *
     * @param name       название.
     * @param index      индекс страницы.
     * @param valueClass класс данных.
     * @param style      стиль.
     */
    public FastExcelSheet(String name, Integer index, Class<T> valueClass, StyleImpl style) {
        super(name, index, valueClass, style);
    }

    @Override
    protected String getSheetName(Sheet workSheet) {
        return workSheet.getName();
    }

    @Override
    protected int getRowCount(Sheet workSheet) {
        return workSheet.getReadableRows().size();
    }

    @Override
    protected BaseHeaderReader<Sheet> createHeaderReader(Sheet workSheet) {
        return new FastExcelHeaderReader(workSheet);
    }

    @Override
    protected BaseDataReader<T, Sheet, Row, Cell> createDataReader(Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        return new FastExcelDataReader<>(workSheet, valueClass, columns);
    }
}
