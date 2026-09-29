package ru.sber.transport.spreadsheet.fastexcel.reader;

import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.DateUtil;
import org.dhatim.fastexcel.reader.CellType;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Row;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Sheet;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Map;

/**
 * Итератор данных FastExcel.
 *
 * @param <T> тип данных.
 */
public class FastExcelDataIterator<T> extends BaseDataIterator<T, Sheet, Row, Cell> {

    /**
     * Создать новый итератор.
     *
     * @param headersCount количество строк-заголовков.
     * @param valueClass класс значения.
     * @param columnsIndices индексы столбцов.
     * @param columnsMap сопоставление столбцов и полей.
     * @param workSheet рабочая страница.
     * @param validator валидатор.
     * @param ignoreFormulas флаг игнорирования формул.
     */
    public FastExcelDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        super(headersCount + 1, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected Row getRow(Sheet workSheet, int rowNum) {
        return workSheet.getReadableRows().get(rowNum - 1);
    }

    @Override
    protected int getRowsCount(Sheet workSheet) {
        return workSheet.getReadableRows().size();
    }

    @Override
    protected int getRowNumber(Row row) {
        return row.getIndex();
    }

    @Override
    protected int getLastCellNumber(Row row) {
        return row.getReadableCellCount();
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return row.getReadableCell(index);
    }

    @Override
    protected boolean isBlank(Cell cell) {
        var value = cell.getRawValue();
        return value == null || value.isBlank();
    }

    @Override
    protected boolean isFormula(Cell cell) {
        return cell.getType().equals(CellType.FORMULA);
    }

    @Override
    protected boolean isNumber(Cell cell) {
        return cell.getType().equals(CellType.NUMBER);
    }

    @Override
    protected String getString(Cell cell) {
        return cell.getText();
    }

    @Override
    protected Long getLong(Cell cell) {
        var number = cell.getNumber();
        return number == null ? null : number.longValue();
    }

    @Override
    protected Integer getInteger(Cell cell) {
        var number = cell.getNumber();
        return number == null ? null : number.intValue();
    }

    @Override
    protected Double getDouble(Cell cell) {
        var number = cell.getNumber();
        return number == null ? null : number.doubleValue();
    }

    @Override
    protected Boolean getBoolean(Cell cell) {
        return cell.getBoolean();
    }

    @Override
    protected <D> D parseChrono(Cell cell, NullRender nullRender, TimeFormat timeFormat, TemporalQuery<D> query,
                                DateTimeFormatter iso) {
        if (CellType.NUMBER.equals(cell.getType())) {
            var numValue = cell.getNumber();
            var q = DateUtil.getLocalDateTime(numValue.doubleValue(), false);
            return query.queryFrom(q);
        }

        String value = null;
        if (CellType.STRING.equals(cell.getType())) {
            value = cell.getText();
        }

        return parseChrono(value, nullRender, timeFormat, query, iso);
    }
}
