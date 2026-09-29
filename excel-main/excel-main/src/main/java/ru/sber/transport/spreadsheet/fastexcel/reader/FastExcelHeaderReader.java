package ru.sber.transport.spreadsheet.fastexcel.reader;

import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.util.List;

/**
 * Читатель заголовков vFastExcel.
 */
public class FastExcelHeaderReader extends BaseHeaderReader<Sheet> {

    /**
     * Создать нового читателя.
     *
     * @param workSheet рабочая страница для чтения.
     */
    public FastExcelHeaderReader(Sheet workSheet) {
        super(workSheet);
    }

    @Override
    protected List<CellRangeAddress> getMergedRegions(Sheet workSheet) {
        return workSheet.getMerged();
    }
}
