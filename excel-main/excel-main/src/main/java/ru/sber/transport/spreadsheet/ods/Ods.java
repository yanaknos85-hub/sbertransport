package ru.sber.transport.spreadsheet.ods;

import com.github.miachm.sods.Sheet;
import ru.sber.transport.spreadsheet.base.SpreadSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;
import ru.sber.transport.spreadsheet.ods.stream.OdsStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class Ods extends SpreadSheet<Ods, OdsSheet<Object>, com.github.miachm.sods.SpreadSheet, Sheet, Row, Cell> {

    @Override
    protected com.github.miachm.sods.SpreadSheet doLoad(InputStream stream) throws IOException {
        return new com.github.miachm.sods.SpreadSheet(stream);
    }

    @Override
    protected Sheet getSheet(com.github.miachm.sods.SpreadSheet workbook, String name) {
        return Optional.ofNullable(workbook.getSheet(name))
            .orElseGet(() -> {
                var newSheet = new Sheet(name);
                workbook.appendSheet(newSheet);
                return newSheet;
            });
    }

    @Override
    protected Sheet getSheet(com.github.miachm.sods.SpreadSheet workbook, Integer index) {
        return Optional.ofNullable(workbook.getSheet(index))
            .orElseGet(() -> {
                var newSheet = new Sheet("Sheet " + index);
                workbook.addSheet(newSheet, index);
                return newSheet;
            });
    }

    @Override
    protected String getSheetName(com.github.miachm.sods.SpreadSheet workbook, Integer index) {
        if (workbook.getNumSheets() <= index) {
            return null;
        }
        return workbook.getSheet(index).getName();
    }

    @Override
    protected <T> OdsSheet<T> createSheet(String name, Class<T> valueClass, StyleImpl style) {
        return new OdsSheet<>(name, valueClass, style);
    }

    @Override
    protected <T> OdsSheet<T> createSheet(String name, int index, Class<T> valueClass, StyleImpl style) {
        return new OdsSheet<>(name, index, valueClass, style);
    }

    @Override
    protected String getTypeName() {
        return "ods";
    }

    @Override
    public BaseWorkbookStream<Ods, OdsSheet<Object>, com.github.miachm.sods.SpreadSheet, Sheet, Row, Cell> openStream(OutputStream stream) {
        return new OdsStream(this, stream, new com.github.miachm.sods.SpreadSheet());
    }
}
