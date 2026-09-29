package ru.sber.transport.spreadsheet.ods.writer;

import com.github.miachm.sods.Sheet;
import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.util.List;

/**
 * Писатель колонтитулов.
 */
public class OdsCaptionWriter extends BaseCaptionWriter<Sheet, Row, Cell> {

    /**
     * Создать новый писатель.
     *
     * @param sheetToWrite страница для записи.
     * @param caption текст колонтитула.
     */
    public OdsCaptionWriter(Sheet sheetToWrite, List<List<String>> caption) {
        super(sheetToWrite, caption);
    }

    @Override
    protected void setValue(Cell cell, String caption) {
        cell.value(caption);
    }

    @Override
    protected Row getRow(Sheet sheet, int index) {
        return new Row(sheet, index);
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return new Cell(row, index);
    }
}
