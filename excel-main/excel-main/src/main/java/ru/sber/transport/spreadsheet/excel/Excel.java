package ru.sber.transport.spreadsheet.excel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import ru.sber.transport.spreadsheet.base.SpreadSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.excel.stream.WorkbookStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Представление excel-файла.
 */
@Slf4j
@RequiredArgsConstructor
public class Excel extends SpreadSheet<Excel, Sheet<Object>, Workbook, org.apache.poi.ss.usermodel.Sheet, Row, Cell> {

    @Getter
    private final WorkbookType type;

    @Override
    protected <T> Sheet<T> createSheet(String name, Class<T> valueClass, StyleImpl style) {
        return new Sheet<>(name, valueClass, style);
    }

    @Override
    protected <T> Sheet<T> createSheet(String name, int index, Class<T> valueClass, StyleImpl style) {
        return new Sheet<>(name, index, valueClass, style);
    }

    /**
     * Загрузить книгу из потока.
     *
     * @param stream поток.
     */
    protected Workbook doLoad(InputStream stream) {
        return type.getLoadWorkbook().apply(stream);
    }

    @Override
    public WorkbookStream openStream(OutputStream stream) {
        return new WorkbookStream(this, stream);
    }

    @Override
    protected org.apache.poi.ss.usermodel.Sheet getSheet(Workbook workbook, String name) {
        return workbook.getSheet(name);
    }

    @Override
    protected org.apache.poi.ss.usermodel.Sheet getSheet(Workbook workbook, Integer index) {
        if (workbook.getNumberOfSheets() <= index) {
            return null;
        }
        return workbook.getSheetAt(index);
    }

    @Override
    protected String getSheetName(Workbook workbook, Integer index) {
        var sheet = getSheet(workbook, index);
        if (sheet == null) {
            return null;
        }
        return sheet.getSheetName();
    }

    @Override
    protected String getTypeName() {
        return type.name();
    }
}
