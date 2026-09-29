package ru.sber.transport.spreadsheet.ods;

import com.github.miachm.sods.Sheet;
import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;
import ru.sber.transport.spreadsheet.ods.reader.OdsDataReader;
import ru.sber.transport.spreadsheet.ods.reader.OdsHeaderReader;

import java.util.List;

public class OdsSheet<T> extends BaseSheet<T, Ods, Sheet, Row, Cell> {

    public OdsSheet(String name, Class<T> valueClass, StyleImpl style) {
        super(name, valueClass, style);
    }

    public OdsSheet(String name, Integer index, Class<T> valueClass, StyleImpl style) {
        super(name, index, valueClass, style);
    }

    @Override
    protected String getSheetName(Sheet workSheet) {
        return workSheet.getName();
    }

    @Override
    protected int getRowCount(Sheet workSheet) {
        return workSheet.getMaxRows();
    }

    @Override
    protected BaseHeaderReader<Sheet> createHeaderReader(Sheet workSheet) {
        return new OdsHeaderReader(workSheet);
    }

    @Override
    protected BaseDataReader<T, Sheet, Row, Cell> createDataReader(Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        return new OdsDataReader<>(workSheet, valueClass, columns);
    }
}
