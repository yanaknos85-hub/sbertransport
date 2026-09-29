package ru.sber.transport.spreadsheet.fastexcel.writer;

import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.fastexcel.FastExcelSheet;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

/**
 * Писатель данных.
 *
 * @param <T> тип данных.
 */
public class FastExcelDataWriter<T> extends BaseDataWriter<T, FastExcelSheet<T>, Sheet, Row, Cell> {

    private int rowsCount = 0;

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param sheet        исходные данные страницы.
     * @param headerRows   количество строк заголовка.
     */
    public FastExcelDataWriter(Sheet sheetToWrite, FastExcelSheet<T> sheet, int headerRows) {
        super(sheetToWrite, sheet, headerRows);
    }

    @Override
    protected void setAutosize(Sheet sheetToWrite, int columnIndex) {
        // FastExcel не поддерживает автоширину колонок, поэтому ничего не делаем.
    }

    @Override
    protected int getRows(Sheet sheetToWrite) {
        return rowsCount;
    }

    @Override
    protected Row getRow(Sheet sheetToWrite, int index) {
        rowsCount++;
        return new Row(sheetToWrite, index, null);
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        return new Cell(row, null, null, null, columnIndex);
    }

    @Override
    protected void setCellValue(Object value, Cell cell, NullRender nullRender, TimeFormat timeFormat) {
        if (value == null && nullRender == null) {
            return;
        }

        if (value == null) {
            cell.setValue(nullRender.value());
        }

        if (value instanceof String) {
            cell.setValue(String.valueOf(value));
        } else if (value instanceof Number number) {
            cell.setValue(number.doubleValue());
        } else {
            if (value instanceof LocalDate date) {
                cell.setValue(date);
            } else if (value instanceof LocalDateTime dateTime) {
                cell.setValue(dateTime);
            } else if (value instanceof LocalTime time) {
                cell.setValue(time.toString());
            } else if (value instanceof Calendar calendar) {
                cell.setValue(calendar);
            } else if (value instanceof Boolean bool) {
                cell.setValue(bool);
            } else if (value instanceof Date date) {
                cell.setValue(date);
            } else if (value instanceof Duration duration) {
                cell.setValue(format(duration, timeFormat));
            }
        }
    }

    @Override
    protected void setBackgroundColor(Cell cell, Color backgroundColor) {
        var sheet = cell.getRow().getSheet();
        var originalSheet = sheet.getWritableSheet();
        var stringColor = Integer.toHexString(backgroundColor.getRGB()).substring(2);
        originalSheet.style(cell.getRow().getIndex(), cell.getIndex()).fillColor(stringColor).set();
    }

    @Override
    protected void setWrapText(Cell cell, boolean wrapText) {
        var sheet = cell.getRow().getSheet();
        var originalSheet = sheet.getWritableSheet();
        originalSheet.style(cell.getRow().getIndex(), cell.getIndex()).wrapText(wrapText).set();
    }

    @Override
    protected void setColumnWidth(Sheet targetSheet, int columnIndex, int width) {
        targetSheet.getWritableSheet().width(columnIndex, (float) width / 256);
    }

    @Override
    protected void setFontColor(Cell cell, Color fontColor) {
        var sheet = cell.getRow().getSheet();
        var originalSheet = sheet.getWritableSheet();
        var stringColor = Integer.toHexString(fontColor.getRGB()).substring(2);
        originalSheet.style(cell.getRow().getIndex(), cell.getIndex()).fontColor(stringColor).set();
    }

    private String format(Duration duration, TimeFormat formatter) {
        if (formatter == null) {
            return duration.toString();
        }
        var format = formatter.value();
        var result = switch (format) {
            case SECONDS -> "" + duration.toSeconds();
            case MINUTES -> "%s:%02d".formatted(duration.toMinutes(), duration.toSecondsPart());
            case HOURS ->
                    "%s:%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            default -> throw new IllegalArgumentException("Format %s is not supported".formatted(format.name()));
        };
        if (formatter.withMillis()) {
            result = "%s.%03d".formatted(result, duration.toMillisPart());
        }
        return result;
    }
}
