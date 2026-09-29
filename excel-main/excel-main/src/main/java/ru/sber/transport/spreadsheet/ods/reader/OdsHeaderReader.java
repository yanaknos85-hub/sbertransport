package ru.sber.transport.spreadsheet.ods.reader;

import com.github.miachm.sods.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.BaseHeaderReader;

import java.util.Arrays;
import java.util.List;

/**
 * Читатель заголовков ODS.
 */
public class OdsHeaderReader extends BaseHeaderReader<Sheet> {

    /**
     * Создать нового читателя.
     *
     * @param workSheet рабочая страница для чтения.
     */
    public OdsHeaderReader(Sheet workSheet) {
        super(workSheet);
    }

    @Override
    protected List<CellRangeAddress> getMergedRegions(Sheet workSheet) {
        return Arrays.stream(workSheet.getDataRange().getMergedCells()).parallel()
            .map(m -> m.getMergedCells()[0])
            .map(m -> new CellRangeAddress(m.getRow(), m.getRow() + m.getLastRow(), m.getColumn(), m.getColumn() + m.getLastColumn()))
            .toList();
    }
}
