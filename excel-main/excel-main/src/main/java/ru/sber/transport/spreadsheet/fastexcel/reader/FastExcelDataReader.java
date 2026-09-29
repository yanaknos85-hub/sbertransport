package ru.sber.transport.spreadsheet.fastexcel.reader;

import jakarta.validation.Validator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dhatim.fastexcel.reader.CellType;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.util.*;

/**
 * Создать новый читатель данных FastExcel.
 *
 * @param <T> тип данных.
 */
public class FastExcelDataReader<T> extends BaseDataReader<T, Sheet, Row, Cell> {

    /**
     * Создать читатель.
     *
     * @param workSheet  рабочая страница. 
     * @param valueClass класс значения.
     * @param columns    список столбцов.
     */
    public FastExcelDataReader(Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        super(workSheet, valueClass, columns);
    }

    @Override
    protected BaseDataIterator<T, Sheet, Row, Cell> createDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        return new FastExcelDataIterator<>(headersCount, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected Row getRow(Sheet workSheet, int index) {
        if (index >= workSheet.getReadableRows().size()) {
            return null;
        }

        return workSheet.getReadableRows().get(index);
    }

    @Override
    protected List<CellRangeAddress> getMerged(Sheet workSheet) {
        return workSheet.getMerged();
    }

    @Override
    protected int getLastCell(Row row) {
        return row.getSheet().getReadableRows().stream().mapToInt(Row::getReadableCellCount).max().orElse(0);
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return row.getReadableCell(index);
    }

    @Override
    protected String getString(Cell cell) {
        return cell.getText();
    }

    @Override
    protected boolean isNotBlank(Cell cell) {
        return !cell.getType().equals(CellType.EMPTY);
    }
}
