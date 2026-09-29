package ru.sber.transport.spreadsheet.fastexcel.abstraction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ExcelReaderException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Абстракция над двумя типами ячеек в fastexcel.
 */
@RequiredArgsConstructor
public class Cell {

    /**
     * Строка.
     */
    @Getter
    private final Row row;

    private final Object readableValue;

    private final String readableRawValue;

    private final CellType readableCellType;

    /**
     * Индекс ячейки в строке.
     */
    @Getter
    private final int index;

    /**
     * Установить новое значение ячейки типа String.
     *
     * @param val новое значение.
     */
    public void setValue(String val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа LocalDateTime.
     *
     * @param val новое значение.
     */
    public void setValue(LocalDateTime val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа Number.
     *
     * @param val новое значение.
     */
    public void setValue(Number val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа LocalDate.
     *
     * @param val новое значение.
     */
    public void setValue(LocalDate val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа Calendar.
     *
     * @param val новое значение.
     */
    public void setValue(Calendar val) {
        setValue(val.getTime());
    }

    /**
     * Установить новое значение ячейки типа Boolean.
     *
     * @param val новое значение.
     */
    public void setValue(Boolean val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа Date.
     *
     * @param val новое значение.
     */
    public void setValue(Date val) {
        row.getSheet().getWritableSheet().value(row.getIndex(), index, val);
    }

    /**
     * Установить новое значение ячейки типа Object.
     *
     * @param val новое значение.
     */
    public void setValue(Object val) {
        if (val != null) {
            setValue(val.toString());
        }
    }

    /**
     * Получить значение ячейки.
     *
     * @return значение ячейки.
     */
    public String getRawValue() {
        return readableRawValue;
    }

    /**
     * Получить тип ячейки.
     *
     * @return тип ячейки.
     */
    public CellType getType() {
        return readableCellType;
    }

    /**
     * Получить значение строковой ячейки.
     *
     * @return значение ячейки.
     */
    public String getText() {
        requireType(CellType.STRING, CellType.EMPTY);
        return readableValue == null ? "" : (String) readableValue;
    }

    /**
     * Получить значение числовой ячейки.
     *
     * @return значение ячейки.
     */
    public BigDecimal getNumber() {
        requireType(CellType.NUMBER, CellType.FORMULA);

        if (readableValue == null) {
            return null;
        }

        if (!BigDecimal.class.isAssignableFrom(readableValue.getClass())) {
            throw new ExcelReaderException("Wrong cell value. Wanted number or numeric result of formula.");
        }
        return (BigDecimal) readableValue;
    }

    /**
     * Получить значение булевой ячейки.
     *
     * @return значение ячейки.
     */
    public boolean getBoolean() {
        requireType(CellType.BOOLEAN);
        return (Boolean) readableValue;
    }

    private void requireType(CellType... requiredType) {
        if (Arrays.stream(requiredType).noneMatch(p -> p.equals(readableCellType))
                && readableCellType != CellType.EMPTY) {
            var typesStr = Arrays.stream(requiredType).map(CellType::toString).collect(Collectors.joining(", "));
            throw new ExcelReaderException("Wrong cell type " + readableCellType + ", wanted one of: " + typesStr);
        }
    }
}
