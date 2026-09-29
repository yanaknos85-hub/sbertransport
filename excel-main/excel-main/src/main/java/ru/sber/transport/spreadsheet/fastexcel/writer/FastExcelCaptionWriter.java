package ru.sber.transport.spreadsheet.fastexcel.writer;

import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.util.List;

/**
 * Писатель колонтитулов.
 */
public class FastExcelCaptionWriter extends BaseCaptionWriter<Sheet, Row, Cell> {

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param caption текст колонтитула.
     */
    public FastExcelCaptionWriter(Sheet sheetToWrite, List<List<String>> caption) {
        super(sheetToWrite, caption);
    }

    @Override
    protected void setValue(Cell cell, String caption) {
        cell.setValue(caption);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        return new Row(sheet, index, null);
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return new Cell(row, null, null, null, index);
    }
}
