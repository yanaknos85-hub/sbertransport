package ru.sber.transport.spreadsheet.base.writer;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.CellStyle;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.excel.Writer;
import ru.sber.transport.spreadsheet.style.Style;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.awt.*;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Базовый класс-писатель.
 *
 * @param <T> тип данных.
 * @param <SS> тип исходной страницы.
 * @param <TS> тип целевой страницы.
 * @param <R> тип строки.
 * @param <C> тип ячейки.
 */
@SuppressWarnings("java:S119")
@Slf4j
public abstract class BaseDataWriter<T, SS extends BaseSheet<T, ?, ?, R, C>, TS, R, C> implements Writer, AutoCloseable {

    @Getter
    private final TS targetSheet;

    private final SS sourceSheet;

    private int currentRow;

    /**
     * Создание нового объекта записи.
     *
     * @param targetSheet целевая страница.
     * @param sourceSheet исходная страница.
     * @param startRow номер строки с которой необходимо начать запись.
     */
    protected BaseDataWriter(TS targetSheet, SS sourceSheet, int startRow) {
        this.targetSheet = targetSheet;
        this.sourceSheet = sourceSheet;
        currentRow = startRow;
    }

    /**
     * Записать данные на страницу.
     */
    public int write() {
        for (var item : sourceSheet.getData()) {
            writeNext(item);
        }
        return currentRow;
    }

    /**
     * Запись элемента на страницу.
     *
     * @param item элемент для записи.
     * @param styles применяемые стили.
     */
    public void writeNext(T item, CellStyle... styles) {
        var index = currentRow++;
        var row = getRow(targetSheet, index);
        var columnIndex = 0;
        if (item != null) {
            var stylesMap = Arrays.stream(styles).map(cs -> new AbstractMap.SimpleEntry<>(cs.getColumn().getName(), cs))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (var column : sourceSheet.getColumns()) {
                columnIndex = writeColumnData(column, item, row, columnIndex, stylesMap.get(column.getName()));
            }
        }
    }

    /**
     * Записать данные в строку.
     *
     * @param column метаданные столбца.
     * @param item элемент для добавления в строку.
     * @param row строка для записи.
     * @param columnIndex индекс столбца.
     *
     * @return конечный индекс столбца.
     */
    @SneakyThrows({ IllegalArgumentException.class, IllegalAccessException.class})
    private int writeColumnData(
        Column<?, ?> column, Object item, R row, int columnIndex, Style style
    ) {
        var field = Optional.ofNullable(column.getField()).map(f -> ReflectionUtils.getField(item.getClass(), f)).orElse(null);
        var extractor = column.getValueExtractor();
        var columnNullRender = Optional.ofNullable(field).map(f -> f.getAnnotation(NullRender.class)).orElse(null);
        var timeFormat = Optional.ofNullable(field).map(f -> f.getAnnotation(TimeFormat.class)).orElse(null);
        var toPrimitive = column.getToPrimitive();
        Object value = null;
        if (extractor != null) {
            value = extractor.apply(ReflectionUtils.cast(item));
        }
        if (column.getField() != null && value == null) {
            var fieldName = column.getField();
            if (item instanceof Map<?, ?> && fieldName.contains(".")) {
                fieldName = fieldName.split("\\.")[1];
            }
            value = getValue(item, fieldName);
        }
        if (toPrimitive != null && value != null) {
            value = toPrimitive.apply(ReflectionUtils.cast(value));
        }
        value = renderValueIfNull(column, columnNullRender, value);
        if (!column.getChildren().isEmpty()) {
            for (var child : column.getChildren()) {
                columnIndex = writeColumnData(child, value != null ? value : item, row, columnIndex, style);
            }
            return columnIndex;
        }
        var cell = createCell(row, column, columnIndex, style);
        setCellValue(value, cell, columnNullRender, timeFormat);
        return columnIndex + 1;
    }

    private C createCell(R row, Column<?, ?> column, int columnIndex, Style cellStyle) {
        var cell = getCell(row, columnIndex);
        var style = column.getStyle().merge(cellStyle);
        if (style.getFontColor() != null) {
            setFontColor(cell, style.getFontColor());
        }
        if (style.getWidth() != null) {
            setColumnWidth(targetSheet, columnIndex, style.getWidth() * 256);
        }
        if (style.getWrapText() != null) {
            setWrapText(cell, style.getWrapText());
        }
        if (style.getBackgroundColor() != null) {
            setBackgroundColor(cell, style.getBackgroundColor());
        }
        return cell;
    }

    private Object renderValueIfNull(Column<?, ?> column, NullRender columnNullRender, Object value) {
        if (columnNullRender != null && value == null) {
            return columnNullRender.value();
        }
        if (column.getNullRender() != null && value == null) {
            return column.getNullRender().get();
        }
        return value;
    }

    /**
     * Получение значения поля.
     *
     * @param source объект для получения данных.
     * @param fieldName название поля для получения данных.
     * @return значение.
     * @throws IllegalAccessException ошибка доступа.
     */
    private Object getValue(Object source, String fieldName) throws IllegalAccessException {
        if (source == null) {
            return null;
        }
        if (source instanceof Map<?, ?>) {
            return ReflectionUtils.castObjectToMap(source, String.class, Object.class).get(fieldName);
        }
        var fieldParts = fieldName.split("\\.");
        var currentField = fieldParts[0];
        var field = ReflectionUtils.getField(source.getClass(), currentField);
        if (field == null) {
            return null;
        }
        if (!field.canAccess(source)) {
            field.setAccessible(true); // NOSONAR reflective operation is needed here
        }
        var result = field.get(source);
        if (fieldParts.length > 1) {
            result = getValue(result, fieldName.replaceFirst(String.format("%s\\.", currentField), ""));
        }
        return result;
    }

    @Override
    public void close() {
        autosizeColumns(targetSheet);
    }

    /**
     * Mark columns at the sheet as autosized.
     *
     * @param sheetToWrite sheet to set columns.
     */
    private void autosizeColumns(TS sheetToWrite) {
        if (getRows(sheetToWrite) > 0) {
            var columns = sourceSheet.getColumns();
            for (var i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                if (Boolean.TRUE.equals(column.getStyle().getAutosize())) {
                    setAutosize(sheetToWrite, i);
                }
            }
        }
    }

    /**
     * Установить признак автосайза.
     *
     * @param sheetToWrite страница для записи.
     * @param columnIndex индекс столбца.
     */
    protected abstract void setAutosize(TS sheetToWrite, int columnIndex);

    /**
     * Получить количество строк.
     *
     * @param sheetToWrite страница для записи.
     * @return количество строк.
     */
    protected abstract int getRows(TS sheetToWrite);

    /**
     * Получить строку.
     *
     * @param sheetToWrite страница для записи.
     * @param index индекс строки.
     * @return строка.
     */
    protected abstract R getRow(TS sheetToWrite, int index);

    /**
     * Установить значение ячейки.
     *
     * @param value значение.
     * @param cell ячейка.
     * @param nullRender рендер для значения null.
     * @param timeFormat формат даты.
     */
    protected abstract void setCellValue(Object value, C cell, NullRender nullRender, TimeFormat timeFormat);

    /**
     * Получить ячейку.
     *
     * @param row строка.
     * @param columnIndex индекс ячейки.
     * @return ячейка.
     */
    protected abstract C getCell(R row, int columnIndex);

    /**
     * Установить цвет фона.
     *
     * @param cell ячейка.
     * @param backgroundColor цвет.
     */
    protected abstract void setBackgroundColor(C cell, Color backgroundColor);

    /**
     * Установить перенос текста.
     *
     * @param cell ячейка.
     * @param wrapText перенос.
     */
    protected abstract void setWrapText(C cell, boolean wrapText);

    /**
     * Установить ширину столбца.
     *
     * @param targetSheet целевая страница.
     * @param columnIndex индекс столбца.
     * @param width ширина.
     */
    protected abstract void setColumnWidth(TS targetSheet, int columnIndex, int width);

    /**
     * Установить цвет шрифта.
     *
     * @param cell ячейка.
     * @param fontColor цвет шрифта.
     */
    protected abstract void setFontColor(C cell, Color fontColor);

}
