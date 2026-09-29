package ru.sber.transport.spreadsheet.excel.reader;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.base.reader.BaseDataReader;
import ru.sber.transport.spreadsheet.excel.Column;

import java.util.List;
import java.util.Map;

/**
 * Чтение данных из Excel-книги.
 *
 * @param <T> тип данных.
 */
@Slf4j
public class DataReader<T> extends BaseDataReader<T, org.apache.poi.ss.usermodel.Sheet, Row, Cell> {

    public DataReader(Sheet workSheet, Class<T> valueClass, List<Column<T, Object>> columns) {
        super(workSheet, valueClass, columns);
    }

    @Override
    protected BaseDataIterator<T, Sheet, Row, Cell> createDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        return new DataIterator<>(headersCount, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected Row getRow(Sheet workSheet, int index) {
        return workSheet.getRow(index);
    }

    @Override
    protected List<CellRangeAddress> getMerged(Sheet workSheet) {
        return workSheet.getMergedRegions();
    }

    @Override
    protected int getLastCell(Row row) {
        return row.getLastCellNum();
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return row.getCell(index);
    }

    @Override
    protected String getString(Cell cell) {
        return cell.getStringCellValue();
    }

    @Override
    protected boolean isNotBlank(Cell cell) {
        return cell.getCellType() != CellType.BLANK;
    }
}
