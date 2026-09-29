package ru.sber.transport.spreadsheet.excel.reader;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.excel.Column;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Map;

@Slf4j
public class DataIterator<T> extends BaseDataIterator<T, Sheet, Row, Cell> {

    public DataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        super(headersCount, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected int getRowsCount(Sheet workSheet) {
        return workSheet.getLastRowNum();
    }

    @Override
    protected Row getRow(Sheet workSheet, int rowIndex) {
        return workSheet.getRow(rowIndex);
    }

    @Override
    protected int getRowNumber(Row row) {
        return row.getRowNum();
    }

    @Override
    protected int getLastCellNumber(Row row) {
        return row.getLastCellNum();
    }

    @Override
    protected void prepareWorksheet(Sheet workSheet) {
        workSheet.getWorkbook().setForceFormulaRecalculation(true);
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return row.getCell(index);
    }

    @Override
    protected String getString(Cell cell) {
        return cell.toString();
    }

    @Override
    protected boolean isNumber(Cell cell) {
        return cell.getCellType().equals(CellType.NUMERIC);
    }

    @Override
    protected Boolean getBoolean(Cell cell) {
        return cell.getBooleanCellValue();
    }

    @Override
    protected boolean isFormula(Cell cell) {
        return CellType.FORMULA.equals(cell.getCellType());
    }

    @Override
    protected Long getLong(Cell cell) {
        return getDouble(cell).longValue();
    }

    @Override
    protected Double getDouble(Cell cell) {
        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            throw new NumberFormatException();
        }
    }

    @Override
    protected Integer getInteger(Cell cell) {
        return getDouble(cell).intValue();
    }

    @Override
    protected boolean isBlank(Cell cell) {
        return CellType.BLANK.equals(cell.getCellType());
    }

    @Override
    protected <D> D parseChrono(Cell cell, NullRender nullRender, TimeFormat timeFormat, TemporalQuery<D> query, DateTimeFormatter iso) {
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            return query.queryFrom(cell.getLocalDateTimeCellValue());
        }

        String value = null;
        if (CellType.STRING.equals(cell.getCellType())) {
            value = cell.getStringCellValue();
        }

        return parseChrono(value, nullRender, timeFormat, query, iso);
    }
}
