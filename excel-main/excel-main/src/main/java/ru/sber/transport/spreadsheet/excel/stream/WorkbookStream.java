package ru.sber.transport.spreadsheet.excel.stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sber.transport.spreadsheet.excel.writer.CaptionWriter;
import ru.sber.transport.spreadsheet.excel.writer.DataWriter;
import ru.sber.transport.spreadsheet.excel.writer.HeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.Sheet;
import ru.sber.transport.spreadsheet.excel.WorkbookType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Поток записи данных в файл.
 */
public class WorkbookStream extends BaseWorkbookStream<Excel, Sheet<Object>, Workbook, org.apache.poi.ss.usermodel.Sheet, Row, Cell> {

    private final Excel excel;

    /**
     * Создать поток.
     *
     * @param excel таблица.
     * @param stream цель для записи.
     */
    public WorkbookStream(Excel excel, OutputStream stream) {
        super(excel, stream, excel.getType().getCreateWorkbook().get());
        this.excel = excel;
    }

    @Override
    protected Collection<Sheet<Object>> getSheets(Excel source) {
        return source.getSheets().values();
    }

    @Override
    protected org.apache.poi.ss.usermodel.Sheet getSheet(Sheet<Object> sheet, Workbook workbook) {
        var sheetName = sheet.getName();
        var sheetToWrite = workbook.getSheet(sheetName);
        if (sheetToWrite == null) {
            sheetToWrite = workbook.createSheet(sheetName);
        }
        if (WorkbookType.XLSX == excel.getType()) {
            ((SXSSFSheet) sheetToWrite).trackAllColumnsForAutoSizing();
        }
        return sheetToWrite;
    }

    @Override
    protected void close(Workbook workbookToWrite, OutputStream stream) throws IOException {
        workbookToWrite.write(stream);
        workbookToWrite.close();
    }

    @Override
    protected CaptionWriter createCaptionWriter(org.apache.poi.ss.usermodel.Sheet sheetToWrite, List<List<String>> caption) {
        return new CaptionWriter(sheetToWrite, caption);
    }

    @Override
    protected HeaderWriter createHeaderWriter(org.apache.poi.ss.usermodel.Sheet sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl style) {
        return new HeaderWriter(sheetToWrite, columns, style);
    }

    @Override
    protected DataWriter<Object> createDataWriter(org.apache.poi.ss.usermodel.Sheet sheetToWrite, Sheet<Object> sheet, int headerRows) {
        return new DataWriter<>(sheetToWrite, sheet, headerRows);
    }
}
