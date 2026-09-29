package ru.sber.transport.spreadsheet.fastexcel.writer;

import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Писатель заголовков FastExcel.
 */
public class FastExcelHeaderWriter extends BaseHeaderWriter<Sheet, Row, Cell> {

    private int rowCount = 0;

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param columns      список столбцов.
     * @param headerStyle  стиль.
     */
    public FastExcelHeaderWriter(Sheet sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl headerStyle) {
        super(sheetToWrite, columns, headerStyle);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        rowCount++;
        return new Row(sheet, index, null);
    }

    @Override
    protected void setValue(Cell cell, String value) {
        cell.setValue(value);
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        return new Cell(row, null, null, null, columnIndex);
    }

    @Override
    protected void setStyle(Sheet sheet, Cell cell, StyleImpl headerStyle, int columnIndex, Column<?, Object> column) {
        var columnStyle = column.getStyle();
        var styleSetter = sheet.getWritableSheet().style(cell.getRow().getIndex(), cell.getIndex());

        if (columnStyle.getWrapText() != null) {
            styleSetter = styleSetter.wrapText(columnStyle.getWrapText());
        }
        if (columnStyle.getWidth() != null) {
            sheet.getWritableSheet().width(columnIndex, columnStyle.getWidth());
        }

        if (headerStyle.getWrapText() != null) {
            styleSetter = styleSetter.wrapText(headerStyle.getWrapText());
        }
        if (headerStyle.getWidth() != null) {
            sheet.getWritableSheet().width(columnIndex, headerStyle.getWidth());
        }
        if (headerStyle.getBackgroundColor() != null) {
            var stringColor = Integer.toHexString(headerStyle.getBackgroundColor().getRGB()).substring(2);
            styleSetter = styleSetter.fillColor(stringColor);
        }
        if (headerStyle.getBorderStyle() != null) {
            styleSetter = styleSetter.borderStyle(toCamelCase(headerStyle.getBorderStyle().name()));
        }
        if (headerStyle.getBold() != null) {
            styleSetter = styleSetter.bold();
        }
        if (headerStyle.getHorizontalAlignment() != null) {
            styleSetter = styleSetter.horizontalAlignment(toCamelCase(headerStyle.getHorizontalAlignment().name()));
        }
        if (headerStyle.getVerticalAlignment() != null) {
            styleSetter = styleSetter.verticalAlignment(toCamelCase(headerStyle.getVerticalAlignment().name()));
        }

        styleSetter.set();
    }

    @Override
    protected void mergeRegion(int firstRow, int lastRow, int firstColumn, int lastColumn, Sheet sheet, StyleImpl headerStyle, Column<?, Object> column) {
        var originalSheet = sheet.getWritableSheet();
        var styleBuilder = originalSheet
                .range(firstRow, firstColumn, lastRow, lastColumn)
                .style();

        if (headerStyle.getBorderStyle() != null) {
            styleBuilder = styleBuilder.borderStyle(toCamelCase(headerStyle.getBorderStyle().name()));
        }

        styleBuilder.merge().set();
    }

    @Override
    protected void setAutosize(Sheet sheetToWrite, int index) {
        // FastExcel не поддерживает автоширину колонок, поэтому ничего не делаем.
    }

    @Override
    protected int getRows(Sheet sheetToWrite) {
        return rowCount;
    }

    private String toCamelCase(String string) {
        return Pattern.compile("_([a-z])")
                .matcher(string.toLowerCase())
                .replaceAll(m -> m.group(1).toUpperCase());
    }
}
