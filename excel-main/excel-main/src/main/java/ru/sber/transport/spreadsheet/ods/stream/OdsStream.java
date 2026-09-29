package ru.sber.transport.spreadsheet.ods.stream;

import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.Ods;
import ru.sber.transport.spreadsheet.ods.OdsSheet;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;
import ru.sber.transport.spreadsheet.ods.writer.OdsCaptionWriter;
import ru.sber.transport.spreadsheet.ods.writer.OdsDataWriter;
import ru.sber.transport.spreadsheet.ods.writer.OdsHeaderWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class OdsStream extends BaseWorkbookStream<Ods, OdsSheet<Object>, SpreadSheet, Sheet, Row, Cell> {

    public OdsStream(Ods spreadSheet, OutputStream stream, SpreadSheet workbookToWrite) {
        super(spreadSheet, stream, workbookToWrite);
    }

    @Override
    protected void close(SpreadSheet workbookToWrite, OutputStream stream) throws IOException {
        workbookToWrite.save(stream);
        stream.close();
    }

    @Override
    protected Collection<OdsSheet<Object>> getSheets(Ods source) {
        return source.getSheets().values();
    }

    @Override
    protected Sheet getSheet(OdsSheet<Object> sheet, SpreadSheet workbook) {
        return Optional.ofNullable(workbook.getSheet(sheet.getName()))
            .orElseGet(() -> {
                var sh = new Sheet(sheet.getName());
                workbook.appendSheet(sh);
                return sh;
            });
    }

    @Override
    protected BaseCaptionWriter<Sheet, Row, Cell> createCaptionWriter(Sheet sheetToWrite, List<List<String>> caption) {
        return new OdsCaptionWriter(sheetToWrite, caption);
    }

    @Override
    protected BaseHeaderWriter<Sheet, Row, Cell> createHeaderWriter(Sheet sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl headerStyle) {
        return new OdsHeaderWriter(sheetToWrite, columns, headerStyle);
    }

    @Override
    protected BaseDataWriter<Object, OdsSheet<Object>, Sheet, Row, Cell> createDataWriter(Sheet sheetToWrite, OdsSheet<Object> sheet, int headerRows) {
        return new OdsDataWriter<>(sheetToWrite, sheet, headerRows);
    }
}
