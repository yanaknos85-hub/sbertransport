package ru.sber.transport.spreadsheet.ods.writer;

import com.github.miachm.sods.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.util.List;

/**
 * Писатель заголовков ODS.
 */
public class OdsHeaderWriter extends BaseHeaderWriter<Sheet, Row, Cell> {

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param columns список столбцов.
     * @param headerStyle стиль.
     */
    public OdsHeaderWriter(Sheet sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl headerStyle) {
        super(sheetToWrite, columns, headerStyle);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        return new Row(sheet, index);
    }

    @Override
    protected void setValue(Cell cell, String value) {
        cell.value(value);
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        return new Cell(row, columnIndex);
    }

    @Override
    protected void setStyle(Sheet sheet, Cell cell, StyleImpl headerStyle, int columnIndex, Column<?, Object> column) {
        var range = sheet.getRange(cell.row().index(), cell.index(), 1, 1);
        var cellStyle = range.getStyle();
        var columnStyle = column.getStyle();
        doSetStyle(cellStyle, headerStyle);
        doSetStyle(cellStyle, columnStyle);
        if (headerStyle.getWidth() != null) {
            sheet.setColumnWidth(columnIndex, (double) headerStyle.getWidth());
        }
        if (columnStyle.getWidth() != null) {
            sheet.setColumnWidth(columnIndex, (double) columnStyle.getWidth());
        }
        range.setStyle(cellStyle);
    }

    @Override
    protected void mergeRegion(int firstRow, int lastRow, int firstColumn, int lastColumn, Sheet sheet, StyleImpl headerStyle, Column<?, Object> column) {
        var lastColumnNum = lastColumn + 1;
        var lastRowNum = lastRow + 1;
        if (sheet.getMaxColumns() < lastColumnNum) {
            sheet.appendColumns(lastColumnNum - sheet.getMaxColumns());
        }
        if (sheet.getMaxRows() < lastRowNum) {
            sheet.appendRows(lastRowNum - sheet.getMaxRows());
        }
        int numRows = lastRow - firstRow + 1;
        int numColumns = lastColumn - firstColumn + 1;
        if (firstColumn != lastColumn || firstRow != lastRow) {
            var range = sheet.getRange(firstRow, firstColumn, numRows, numColumns);
            var value = range.getValue();
            range.merge();
            var style = range.getStyle();
            doSetStyle(style, headerStyle);
            doSetStyle(style, column.getStyle());
            range.setStyle(style);
            range.setValue(value);
        }
    }

    @Override
    protected void setAutosize(Sheet sheetToWrite, int index) {
        sheetToWrite.getDefaultColumnCellStyle(index).getCssStyles().put("use-optimal-row-height", "true");
    }

    @Override
    protected int getRows(Sheet sheetToWrite) {
        return sheetToWrite.getMaxRows();
    }

    private void doSetStyle(Style cellStyle, StyleImpl columnStyle) {
        if (columnStyle.getWrapText() != null) {
            cellStyle.setWrap(columnStyle.getWrapText());
        }
        if (columnStyle.getBackgroundColor() != null) {
            var backgroundColor = columnStyle.getBackgroundColor();
            cellStyle.setBackgroundColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));
        }
        if (columnStyle.getBorderStyle() != null) {
            var borders = new Borders();
            borders.setBorderTopProperties(getBorderProperties(columnStyle.getBorderStyle()));
            borders.setBorderBottomProperties(getBorderProperties(columnStyle.getBorderStyle()));
            borders.setBorderLeftProperties(getBorderProperties(columnStyle.getBorderStyle()));
            borders.setBorderRightProperties(getBorderProperties(columnStyle.getBorderStyle()));
            cellStyle.setBorders(borders);
        }
        if (columnStyle.getBold() != null) {
            cellStyle.setBold(columnStyle.getBold());
        }
        if (columnStyle.getHorizontalAlignment() != null) {
            cellStyle.setTextAligment(switch (columnStyle.getHorizontalAlignment()) {
                case LEFT, GENERAL, FILL, DISTRIBUTED -> Style.TEXT_ALIGMENT.Left;
                case CENTER, JUSTIFY, CENTER_SELECTION -> Style.TEXT_ALIGMENT.Center;
                case RIGHT -> Style.TEXT_ALIGMENT.Right;
            });
        }
        if (columnStyle.getVerticalAlignment() != null) {
            cellStyle.setVerticalTextAligment(switch (columnStyle.getVerticalAlignment()) {
                case TOP, DISTRIBUTED -> Style.VERTICAL_TEXT_ALIGMENT.Top;
                case BOTTOM -> Style.VERTICAL_TEXT_ALIGMENT.Bottom;
                case JUSTIFY, CENTER -> Style.VERTICAL_TEXT_ALIGMENT.Middle;
            });
        }
    }

    private String getBorderProperties(BorderStyle borderStyle) {
        return switch (borderStyle) {
            case NONE, HAIR -> "none";
            case THIN -> "0.75pt solid #000";
            case MEDIUM -> "1.5pt solid #000";
            case DASHED -> "0.75pt dashed #000";
            case DASH_DOT_DOT -> "0.75pt dash-dot-dot #000";
            case DASH_DOT, SLANTED_DASH_DOT -> "0.75pt dash-dot #000";
            case DOTTED -> "1.5pt dotted #000";
            case THICK -> "2.25pt solid #000";
            case DOUBLE -> "1.5pt double #000";
            case MEDIUM_DASHED -> "1.5pt dashed #000";
            case MEDIUM_DASH_DOT_DOT -> "1.5pt dash-dot-dot #000";
            case MEDIUM_DASH_DOT -> "1.5pt dash-dot #000";
        };
    }
}
