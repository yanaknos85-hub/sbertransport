package ru.sber.transport.spreadsheet.ods.reader;

import com.github.miachm.sods.Sheet;
import jakarta.validation.Validator;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.reader.BaseDataIterator;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Итератор данных ODS.
 *
 * @param <T> тип данных.
 */
public class OdsDataIterator<T> extends BaseDataIterator<T, Sheet, Row, Cell> {

    /**
     * Создать новый итератор.
     *
     * @param headersCount количество строк-заголовков.
     * @param valueClass класс значения.
     * @param columnsIndices индексы столбцов.
     * @param columnsMap сопоставление столбцов и полей.
     * @param keyValueColumns столбцы, представляющие собой карты ключ-значение.
     * @param workSheet рабочая страница.
     * @param validator валидатор.
     * @param ignoreFormulas флаг игнорирования формул.
     */
    public OdsDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, Sheet workSheet, Validator validator, boolean ignoreFormulas) {
        super(headersCount + 1, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    @Override
    protected Row getRow(Sheet workSheet, int rowNum) {
        return new Row(workSheet, rowNum - 1);
    }

    @Override
    protected int getRowsCount(Sheet workSheet) {
        return workSheet.getMaxRows();
    }

    @Override
    protected int getRowNumber(Row row) {
        return row.index();
    }

    @Override
    protected int getLastCellNumber(Row row) {
        return row.sheet().getMaxColumns();
    }

    @Override
    protected Cell getCell(Row row, int index) {
        return new Cell(row, index);
    }

    @Override
    protected boolean isBlank(Cell cell) {
        var value = getString(cell);
        return value == null || value.isBlank();
    }

    @Override
    protected boolean isFormula(Cell cell) {
        return cell.hasFormula();
    }

    @Override
    protected boolean isNumber(Cell cell) {
        return cell.value() instanceof Number;
    }

    @Override
    protected String getString(Cell cell) {
        return Optional.ofNullable(cell.value()).map(String::valueOf).orElse(null);
    }

    @Override
    protected Long getLong(Cell cell) {
        return Optional.ofNullable(cell.value()).map(Double.class::cast).map(Double::longValue).orElse(null);
    }

    @Override
    protected Integer getInteger(Cell cell) {
        return Optional.ofNullable(cell.value()).map(Double.class::cast).map(Double::intValue).orElse(null);
    }

    @Override
    protected Double getDouble(Cell cell) {
        return Optional.ofNullable(cell.value()).map(Double.class::cast).orElse(null);
    }

    @Override
    protected Boolean getBoolean(Cell cell) {
        return Optional.ofNullable(cell.value()).map(Boolean.class::cast).orElse(null);
    }

    @Override
    protected LocalTime getLocalTime(Cell cell, NullRender nullRender, TimeFormat timeFormat) {
        if (cell.value() instanceof Duration duration) {
            return LocalTime.of(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        }

        return super.getLocalTime(cell, nullRender, timeFormat);
    }

    @Override
    protected <D> D parseChrono(Cell cell, NullRender nullRender, TimeFormat timeFormat, TemporalQuery<D> query,
                                DateTimeFormatter iso) {
        if (cell.value() instanceof TemporalAccessor temporalAccessor) {
            return query.queryFrom(temporalAccessor);
        }

        var value = getString(cell);
        return parseChrono(value, nullRender, timeFormat, query, iso);
    }
}
