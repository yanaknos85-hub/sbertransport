package ru.sber.transport.spreadsheet.excel.writer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import ru.sber.transport.spreadsheet.ExcelCellBackgroundUtil;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;

import java.util.List;
import java.util.Optional;

/**
 * Запись заголовков.
 */
public class HeaderWriter extends BaseHeaderWriter<Sheet, Row, Cell> {

    public HeaderWriter(Sheet targetSheet, List<? extends Column<?, Object>> columns, StyleImpl style) {
        super(targetSheet, columns, style);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        return Optional.ofNullable(sheet.getRow(index))
            .orElseGet(() -> sheet.createRow(index));
    }

    @Override
    protected Cell getCell(Row row, int columnIndex) {
        return Optional.ofNullable(row.getCell(columnIndex))
            .orElseGet(() -> row.createCell(columnIndex));
    }

    @Override
    protected void setValue(Cell cell, String value) {
        cell.setCellValue(value);
    }

    @Override
    protected void mergeRegion(int firstRow, int lastRow, int firstColumn, int lastColumn, Sheet sheet, StyleImpl headerStyle, Column<?, Object> column) {
        var region = new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn);
        if (headerStyle.getBorderStyle() != null) {
            RegionUtil.setBorderBottom(headerStyle.getBorderStyle(), region, sheet);
            RegionUtil.setBorderLeft(headerStyle.getBorderStyle(), region, sheet);
            RegionUtil.setBorderTop(headerStyle.getBorderStyle(), region, sheet);
            RegionUtil.setBorderRight(headerStyle.getBorderStyle(), region, sheet);
        }
        sheet.addMergedRegion(region);
    }

    @Override
    protected void setStyle(Sheet sheet, Cell cell, StyleImpl headerStyle, int columnIndex, Column<?, Object> column) {
        var cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        var columnStyle = column.getStyle();

        if (columnStyle.getWrapText() != null) {
            cellStyle.setWrapText(columnStyle.getWrapText());
        }
        if (columnStyle.getWidth() != null) {
            sheet.setColumnWidth(columnIndex, columnStyle.getWidth());
        }

        if (headerStyle.getWrapText() != null) {
            cellStyle.setWrapText(headerStyle.getWrapText());
        }
        if (headerStyle.getWidth() != null) {
            sheet.setColumnWidth(columnIndex, headerStyle.getWidth());
        }
        if (headerStyle.getBackgroundColor() != null) {
            var backgroundColor = headerStyle.getBackgroundColor();
            ExcelCellBackgroundUtil.setCellBackground(cellStyle, backgroundColor);
        }
        if (headerStyle.getBorderStyle() != null) {
            cellStyle.setBorderBottom(headerStyle.getBorderStyle());
            cellStyle.setBorderLeft(headerStyle.getBorderStyle());
            cellStyle.setBorderTop(headerStyle.getBorderStyle());
            cellStyle.setBorderRight(headerStyle.getBorderStyle());
        }
        if (headerStyle.getBold() != null) {
            var font = cell.getSheet().getWorkbook().createFont();
            font.setBold(headerStyle.getBold());
            cellStyle.setFont(font);
        }
        if (headerStyle.getHorizontalAlignment() != null) {
            cellStyle.setAlignment(headerStyle.getHorizontalAlignment());
        }
        if (headerStyle.getVerticalAlignment() != null) {
            cellStyle.setVerticalAlignment(headerStyle.getVerticalAlignment());
        }

        cell.setCellStyle(cellStyle);
    }

    @Override
    protected int getRows(Sheet sheetToWrite) {
        return sheetToWrite.getPhysicalNumberOfRows();
    }

    @Override
    protected void setAutosize(Sheet sheetToWrite, int index) {
        sheetToWrite.autoSizeColumn(index, true);
    }
}
