package ru.sber.transport.spreadsheet.ods.writer;

import com.github.miachm.sods.Sheet;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.ods.OdsSheet;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.awt.*;
import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Писатель данных.
 *
 * @param <T> тип данных.
 */
public class OdsDataWriter<T> extends BaseDataWriter<T, OdsSheet<T>, Sheet, Row, Cell> {

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param sheet исходные данные страницы.
     * @param headerRows количество строк заголовка.
     */
    public OdsDataWriter(Sheet sheetToWrite, OdsSheet<T> sheet, int headerRows) {
        super(sheetToWrite, sheet, headerRows);
    }

    @Override
    protected void setAutosize(Sheet sheetToWrite, int columnIndex) {
        sheetToWrite.getDefaultColumnCellStyle(columnIndex).getCssStyles().put("use-optimal-row-height", "true");
    }

    @Override
    protected int getRows(Sheet sheetToWrite) {
        return sheetToWrite.getMaxRows();
    }

    @Override
    protected Row getRow(Sheet sheetToWrite, int index) {
        return new Row(sheetToWrite, index);
    }

    @Override
    protected void setCellValue(Object value, Cell cell, NullRender nullRender, TimeFormat timeFormat) {
        if (value == null && nullRender != null) {
            value = nullRender.value();
        }
        if (value instanceof GregorianCalendar calendar) {
            cell.value( calendar.toZonedDateTime().toLocalDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        } else if (value instanceof Date date) {
            cell.value(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        } else {
            cell.value(value);
        }
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        return new Cell(row, columnIndex);
    }

    @Override
    protected void setBackgroundColor(Cell cell, Color backgroundColor) {
        var sheet = cell.row().sheet();
        var index = cell.index();
        var range = sheet.getRange(cell.row().index(), index, 1, 1);
        var style = range.getStyle();
        style.setBackgroundColor(new com.github.miachm.sods.Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()));
        range.setStyle(style);
    }

    @Override
    protected void setWrapText(Cell cell, boolean wrapText) {
        var sheet = cell.row().sheet();
        var index = cell.index();
        var range = sheet.getRange(cell.row().index(), index, 1, 1);
        var style = range.getStyle();
        style.setWrap(wrapText);
        range.setStyle(style);
    }

    @Override
    protected void setColumnWidth(Sheet targetSheet, int columnIndex, int width) {
        targetSheet.setColumnWidth(columnIndex, (double) width);
    }

    @Override
    protected void setFontColor(Cell cell, Color fontColor) {
        var sheet = cell.row().sheet();
        var index = cell.index();
        var range = sheet.getRange(cell.row().index(), index, 1, 1);
        var style = range.getStyle();
        style.setFontColor(new com.github.miachm.sods.Color(fontColor.getRed(), fontColor.getGreen(), fontColor.getBlue()));
        range.setStyle(style);
    }

}
