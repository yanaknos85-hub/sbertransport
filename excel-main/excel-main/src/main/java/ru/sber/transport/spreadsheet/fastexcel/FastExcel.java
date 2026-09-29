package ru.sber.transport.spreadsheet.fastexcel;

import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.SpreadSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Workbook;
import ru.sber.transport.spreadsheet.fastexcel.stream.FastExcelStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

/**
 * Рабочая книга для работы с FastExcel.
 */
public class FastExcel extends SpreadSheet<FastExcel, FastExcelSheet<Object>, Workbook, Sheet, Row, Cell> {

    @Override
    protected Workbook doLoad(InputStream stream) throws IOException {
        return new Workbook(stream);
    }

    @Override
    protected Sheet getSheet(Workbook workbook, String name) {
        return workbook.getSheet(name, true);
    }

    @Override
    protected Sheet getSheet(Workbook workbook, Integer index) {
        return workbook.getSheet(index);
    }

    @Override
    protected String getSheetName(Workbook workbook, Integer index) {
        try {
            return workbook.getSheet(index).getName();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    protected <T> BaseSheet<T, FastExcel, Sheet, Row, Cell> createSheet(String name, Class<T> valueClass, StyleImpl style) {
        return new FastExcelSheet<>(name, valueClass, style);
    }

    @Override
    protected <T> BaseSheet<T, FastExcel, Sheet, Row, Cell> createSheet(String name, int index, Class<T> valueClass, StyleImpl style) {
        return new FastExcelSheet<>(name, index, valueClass, style);
    }

    @Override
    protected String getTypeName() {
        return "xlsx";
    }

    @Override
    public BaseWorkbookStream<FastExcel, FastExcelSheet<Object>, Workbook, Sheet, Row, Cell> openStream(OutputStream stream) {
        return new FastExcelStream(this, stream, new Workbook(stream));
    }
}
