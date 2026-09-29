package ru.sber.transport.spreadsheet.excel.writer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;

import java.util.List;
import java.util.Optional;


/**
 * Запись Верхнего колонтитула.
 */
public class CaptionWriter extends BaseCaptionWriter<Sheet, Row, Cell> {

    public CaptionWriter(Sheet targetSheet, List<List<String>> attention) {
        super(targetSheet, attention);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        return Optional.ofNullable(sheet.getRow(0))
            .orElseGet(() -> sheet.createRow(0));
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return Optional.ofNullable(row.getCell(0))
            .orElseGet(() -> row.createCell(0));
    }

    @Override
    protected void setValue(Cell cell, String caption) {
        cell.setCellValue(caption);
    }

    @Override
    protected void postProcess(Cell cell) {
        var cellStyle = cell.getSheet().getWorkbook().createCellStyle();

        var font = cell.getSheet().getWorkbook().createFont();
        font.setBold(true);
        font.setColor(Font.COLOR_RED);
        font.setFontHeightInPoints((short) 24);

        cellStyle.setFont(font);

        cell.setCellStyle(cellStyle);
    }
}
