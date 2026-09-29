package ru.sber.transport.spreadsheet.ods.reader;

import com.github.miachm.sods.Sheet;
import jakarta.validation.Validator;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Создать новый читатель данных ODS.
 *
 * @param <T> тип данных.
 */
public class OdsDataReader<T> extends BaseDataReader<T, Sheet, Row, Cell> {

    /**
     * Создать читатель.
     *
     * @param workSheet рабочая страница.
     * @param valueClass класс значения.
     * @param columns список столбцов.
     */
    public OdsDataReader(Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        super(workSheet, valueClass, columns);
    }

    @Override
    protected BaseDataIterator<T, Sheet, Row, Cell> createDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        return new OdsDataIterator<>(headersCount, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected Row getRow(Sheet workSheet, int index) {
        return new Row(workSheet, index);
    }

    @Override
    protected List<CellRangeAddress> getMerged(Sheet workSheet) {
        var merged = workSheet.getDataRange().getMergedCells();
        var addresses = new ArrayList<CellRangeAddress>();
        for (var mergedRegion : merged) {
            var cells = mergedRegion.getMergedCells()[0];
            addresses.add(new CellRangeAddress(cells.getRow(), cells.getLastRow(), cells.getColumn(), cells.getLastColumn()));
        }
        return addresses;
    }

    @Override
    protected int getLastCell(Row row) {
        return row.sheet().getMaxColumns();
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return new Cell(row, index);
    }

    @Override
    protected String getString(Cell cell) {
        return Optional.ofNullable(cell.value()).map(String::valueOf).orElse(null);
    }

    @Override
    protected boolean isNotBlank(Cell cell) {
        var value = getString(cell);
        return value != null && !value.isBlank();
    }
}
