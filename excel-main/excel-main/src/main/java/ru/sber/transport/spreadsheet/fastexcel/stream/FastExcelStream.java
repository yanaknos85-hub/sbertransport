package ru.sber.transport.spreadsheet.fastexcel.stream;

import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.fastexcel.FastExcel;
import ru.sber.transport.spreadsheet.fastexcel.FastExcelSheet;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Workbook;
import ru.sber.transport.spreadsheet.fastexcel.writer.FastExcelCaptionWriter;
import ru.sber.transport.spreadsheet.fastexcel.writer.FastExcelDataWriter;
import ru.sber.transport.spreadsheet.fastexcel.writer.FastExcelHeaderWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Поток записи данных в файл.
 */
public class FastExcelStream extends BaseWorkbookStream<FastExcel, FastExcelSheet<Object>, Workbook, Sheet, Row, Cell> {

    /**
     * Создать поток.
     *
     * @param spreadSheet таблица.
     * @param stream цель для записи.
     * @param workbookToWrite рабочая книга для записи.
     */
    public FastExcelStream(FastExcel spreadSheet, OutputStream stream, Workbook workbookToWrite) {
        super(spreadSheet, stream, workbookToWrite);
    }

    @Override
    protected void close(Workbook workbookToWrite, OutputStream stream) throws IOException {
        workbookToWrite.save();
        workbookToWrite.close();
    }

    @Override
    protected Collection<FastExcelSheet<Object>> getSheets(FastExcel source) {
        return source.getSheets().values();
    }

    @Override
    protected Sheet getSheet(FastExcelSheet<Object> sheet, Workbook workbook) {
        return workbook.getSheet(sheet.getName(), false);
    }

    @Override
    protected BaseCaptionWriter<Sheet, Row, Cell> createCaptionWriter(Sheet sheetToWrite, List<List<String>> caption) {
        return new FastExcelCaptionWriter(sheetToWrite, caption);
    }

    @Override
    protected BaseHeaderWriter<Sheet, Row, Cell> createHeaderWriter(Sheet sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl headerStyle) {
        return new FastExcelHeaderWriter(sheetToWrite, columns, headerStyle);
    }

    @Override
    protected BaseDataWriter<Object, FastExcelSheet<Object>, Sheet, Row, Cell> createDataWriter(Sheet sheetToWrite, FastExcelSheet<Object> sheet, int headerRows) {
        return new FastExcelDataWriter<>(sheetToWrite, sheet, headerRows);
    }
}
