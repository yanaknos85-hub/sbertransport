package ru.sber.transport.spreadsheet.excel.writer;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellPropertyType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFColor;
import ru.sber.transport.spreadsheet.ExcelCellBackgroundUtil;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.excel.Sheet;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Запись данных.
 */
@Slf4j
public class DataWriter<T> extends BaseDataWriter<T, Sheet<T>, org.apache.poi.ss.usermodel.Sheet, Row, Cell> {

    private static final String BUILTIN_SHORT_DATE_TIME = "m/d/yy h:mm";

    private static final String BUILTIN_SHORT_TIME = "h:mm";

    private static final String BUILTIN_SHORT_DATE = "m/d/yy";

    private final CellStyle defaultStyle;

    private final Map<Integer, CellStyle> styles = new HashMap<>();

    /**
     * Создание нового объекта записи.
     *
     * @param targetSheet целевая страница.
     * @param sourceSheet исходная страница.
     * @param startRow    номер строки с которой необходимо начать запись.
     */
    public DataWriter(org.apache.poi.ss.usermodel.Sheet targetSheet, Sheet<T> sourceSheet, int startRow) {
        super(targetSheet, sourceSheet, startRow);
        this.defaultStyle = targetSheet.getWorkbook().createCellStyle();
    }

    @Override
    protected Row getRow(org.apache.poi.ss.usermodel.Sheet targetSheet, int index) {
        return Optional.ofNullable(targetSheet.getRow(index))
                .orElseGet(() -> targetSheet.createRow(index));
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        var cell =  Optional.ofNullable(row.getCell(columnIndex))
                .orElseGet(() -> row.createCell(columnIndex));
        cell.setCellStyle(defaultStyle);
        return cell;
    }

    @Override
    protected void setCellValue(Object value, Cell cell, NullRender nullRender, TimeFormat timeFormat) {
        if (value == null && nullRender == null) {
            return;
        }

        if (value == null) {
            cell.setCellValue(nullRender.value());
        }

        if (value instanceof String) {
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            var dataFormat = cell.getRow().getSheet().getWorkbook().createDataFormat();
            short format = dataFormat.getFormat("@");
            if (value instanceof LocalDate date) {
                cell.setCellValue(date);
                format = dataFormat.getFormat(BUILTIN_SHORT_DATE);
            } else if (value instanceof LocalDateTime dateTime) {
                cell.setCellValue(dateTime);
                format = dataFormat.getFormat(BUILTIN_SHORT_DATE_TIME);
            } else if (value instanceof LocalTime time) {
                cell.setCellValue(time.toString());
                format = dataFormat.getFormat(BUILTIN_SHORT_TIME);
            } else if (value instanceof Calendar calendar) {
                cell.setCellValue(calendar);
                format = dataFormat.getFormat(BUILTIN_SHORT_DATE_TIME);
            } else if (value instanceof Boolean bool) {
                cell.setCellValue(bool);
            } else if (value instanceof Date date) {
                cell.setCellValue(date);
                format = dataFormat.getFormat(BUILTIN_SHORT_DATE_TIME);
            } else if (value instanceof Duration duration) {
                cell.setCellValue(format(duration, timeFormat));
                format = dataFormat.getFormat(BUILTIN_SHORT_TIME);
            }
            var style = styles.computeIfAbsent(cell.getColumnIndex(), c -> cell.getRow().getSheet().getWorkbook().createCellStyle());
            style.setDataFormat(format);
            cell.setCellStyle(style);
        }
    }

    @Override
    protected void setColumnWidth(org.apache.poi.ss.usermodel.Sheet targetSheet, int columnIndex, int width) {
        targetSheet.setColumnWidth(columnIndex, width);
    }

    @Override
    protected void setWrapText(Cell cell, boolean wrapText) {
        CellUtil.setCellStyleProperty(cell, CellPropertyType.WRAP_TEXT, wrapText);
    }

    @Override
    protected void setBackgroundColor(Cell cell, Color backgroundColor) {
        var style = cell.getSheet().getWorkbook().createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        ExcelCellBackgroundUtil.setCellBackground(style, backgroundColor);
        cell.setCellStyle(style);
    }

    @Override
    protected void setFontColor(Cell cell, Color fontColor) {
        var workbook = cell.getRow().getSheet().getWorkbook();
        var currentStyle = workbook.createCellStyle();
        currentStyle.cloneStyleFrom(cell.getCellStyle());
        var currentFont = workbook.createFont();
        var bytes = new byte[]{(byte) fontColor.getRed(), (byte) fontColor.getBlue(), (byte) fontColor.getBlue()};
        currentFont.setColor(new XSSFColor(bytes).getIndexed());
        currentStyle.setFont(currentFont);
        cell.setCellStyle(currentStyle);
    }

    @Override
    protected int getRows(org.apache.poi.ss.usermodel.Sheet sheetToWrite) {
        return sheetToWrite.getPhysicalNumberOfRows();
    }

    @Override
    protected void setAutosize(org.apache.poi.ss.usermodel.Sheet sheetToWrite, int columnIndex) {
        sheetToWrite.autoSizeColumn(columnIndex, true);
    }

    private String format(Duration duration, TimeFormat formatter) {
        if (formatter == null) {
            return duration.toString();
        }
        var format = formatter.value();
        var result = switch (format) {
            case SECONDS -> "" + duration.toSeconds();
            case MINUTES -> "%s:%02d".formatted(duration.toMinutes(), duration.toSecondsPart());
            case HOURS -> "%s:%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            default -> throw new IllegalArgumentException("Format %s is not supported".formatted(format.name()));
        };
        if (formatter.withMillis()) {
            result = "%s.%03d".formatted(result, duration.toMillisPart());
        }
        return result;
    }
}
