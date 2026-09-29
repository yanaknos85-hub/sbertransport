package ru.sber.transport.spreadsheet.excel.reader;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;

import java.util.List;

/**
 * Чтение заголовков таблицы.
 */
public class HeaderReader extends BaseHeaderReader<Sheet> {

    public HeaderReader(Sheet workSheet) {
        super(workSheet);
    }

    @Override
    protected List<CellRangeAddress> getMergedRegions(Sheet workSheet) {
        return workSheet.getMergedRegions();
    }
}
