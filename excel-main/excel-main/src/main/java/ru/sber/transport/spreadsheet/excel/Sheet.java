package ru.sber.transport.spreadsheet.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;
import ru.sber.transport.spreadsheet.excel.reader.DataReader;
import ru.sber.transport.spreadsheet.excel.reader.HeaderReader;

import java.util.List;

/**
 * Sheet with data.
 *
 * @param <T> type of items.
 */
public class Sheet<T> extends BaseSheet<T, Excel, org.apache.poi.ss.usermodel.Sheet, Row, Cell> {

    Sheet(String name, Integer index, Class<T> valueClass, StyleImpl headerStyle) {
        super(name, index, valueClass, headerStyle);
    }

    Sheet(String name, Class<T> valueClass, StyleImpl headerStyle) {
        super(name, valueClass, headerStyle);
    }

    @Override
    protected String getSheetName(org.apache.poi.ss.usermodel.Sheet workSheet) {
        return workSheet.getSheetName();
    }

    @Override
    public int getRowCount(org.apache.poi.ss.usermodel.Sheet workSheet) {
        return workSheet.getPhysicalNumberOfRows();
    }

    @Override
    protected BaseHeaderReader<org.apache.poi.ss.usermodel.Sheet> createHeaderReader(org.apache.poi.ss.usermodel.Sheet workSheet) {
        return new HeaderReader(workSheet);
    }

    @Override
    protected BaseDataReader<T, org.apache.poi.ss.usermodel.Sheet, Row, Cell> createDataReader(org.apache.poi.ss.usermodel.Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        return new DataReader<>(workSheet, valueClass, columns);
    }
}
