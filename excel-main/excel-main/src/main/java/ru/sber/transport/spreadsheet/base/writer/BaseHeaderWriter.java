package ru.sber.transport.spreadsheet.base.writer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.excel.Writer;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Базовый писатель заголовков.
 *
 * @param <TS> целевая страница.
 * @param <R> тип строки.
 * @param <C> тип столбца.
 */
@SuppressWarnings("java:S119")
@RequiredArgsConstructor
public abstract class BaseHeaderWriter<TS, R, C> implements Writer, AutoCloseable {

    private final TS targetSheet;

    private final List<? extends Column<?, Object>> columns;

    private final StyleImpl style;

    @Setter
    private int startRow = 0;

    @Override
    public int write() {
        var maxLevel = getMaxLevel(startRow, columns);
        return writeHeader(targetSheet, columns, style, startRow, maxLevel, 0);
    }

    @Override
    public void close() {
        autosizeColumns(targetSheet, style, columns);
    }

    private void autosizeColumns(TS sheetToWrite, StyleImpl style, List<? extends Column<?, Object>> columns) {
        if (getRows(sheetToWrite) > 0 && style.getAutosize() != null && Boolean.TRUE.equals(style.getAutosize())) {
            var i = 0;
            for (var column : columns) {
                i = autosizeColumns(sheetToWrite, column, i);
            }
        }
    }

    private int autosizeColumns(TS sheetToWrite, Column<?, ?> column, int i) {
        var children = column.getChildren();

        if (children.isEmpty()) {
            setAutosize(sheetToWrite, i);
            return ++i;
        }

        for (var child : children) {
            i = autosizeColumns(sheetToWrite, child, i);
        }

        return i;
    }


    /**
     * Записать заголовок.
     *
     * @param sheet      страница для записи заголовка.
     * @param columns    метаданные столбцов.
     * @param level      уровень заголовка.
     * @param maxLevel   максимальный уровень заголовков.
     * @param startIndex начальный индекс столбца.
     * @return количество добавленных строк.
     */
    private int writeHeader(
        TS sheet, List<? extends Column<?, Object>> columns, StyleImpl headerStyle, int level, int maxLevel, int startIndex
    ) {
        var row = getRow(sheet, level);
        writeColumns(sheet, columns, headerStyle, level, maxLevel, row, startIndex);
        return maxLevel;
    }

    private void writeColumns(TS sheet, List<? extends Column<?, Object>> columns, StyleImpl headerStyle, int level, int maxLevel, R row, int columnIndex) {
        for (var column : columns) {
            var cell = getCell(row, columnIndex);
            setValue(cell, column.getName());

            setStyle(sheet, cell, headerStyle, columnIndex, column);

            var count = getAllChildrenCount(column);
            if (count > 0) {
                var lastColumn = columnIndex + count - 1;
                if (lastColumn > columnIndex) {
                    mergeRegion(level, level, columnIndex, columnIndex + count - 1, sheet, headerStyle, column);
                }
            }
            if (!column.getChildren().isEmpty()) {
                writeHeader(sheet, column.getChildren(), headerStyle, level + 1, maxLevel, columnIndex);
            }
            if (maxLevel - level > 1 && column.getChildren().isEmpty()) {
                mergeRegion(level, maxLevel - 1, columnIndex, columnIndex, sheet, headerStyle, column);
            }
            columnIndex += count > 0 ? count : 1;
        }
    }

    /**
     * Получение количества детей.
     *
     * @param column метаданные столбцов.
     * @return количество детей.
     */
    private int getAllChildrenCount(Column<?, ?> column) {
        var children = column.getChildren();
        var count = children.size();
        for (var child : children) {
            var childrenCount = getAllChildrenCount(child);
            if (childrenCount > 0) {
                childrenCount--;
            }
            count += childrenCount;
        }
        return count;
    }

    /**
     * Получить максимальный уровень заголовка.
     *
     * @param curLevel текущий уровень.
     * @param columns  метаданные столбцов.
     * @return максимальный уровень.
     */
    private int getMaxLevel(int curLevel, List<? extends Column<?, Object>> columns) {
        var columnLevels = new LinkedHashMap<Column<?, ?>, Integer>();
        for (var column : columns) {
            var children = column.getChildren();
            columnLevels.put(column, getMaxLevel(curLevel + 1, children));
        }
        return columnLevels.values().stream().mapToInt(o -> o).max().orElse(curLevel);
    }

    /**
     * Получить строку.
     *
     * @param sheet страница.
     * @param index индекс строки.
     * @return строка.
     */
    protected abstract R getRow(TS sheet, int index);

    /**
     * Установка значения.
     *
     * @param cell ячейка.
     * @param value значение.
     */
    protected abstract void setValue(C cell, String value);

    /**
     * Получение ячейки.
     *
     * @param row строка.
     * @param columnIndex индекс столбца.
     * @return получение ячейки.
     */
    protected abstract C getCell(R row, int columnIndex);

    /**
     * Установка стиля.
     *
     * @param sheet страница.
     * @param cell ячейка.
     * @param style стиль.
     * @param columnIndex индекс столбца.
     * @param column столбец.
     */
    protected abstract void setStyle(TS sheet, C cell, StyleImpl style, int columnIndex, Column<?, Object> column);

    /**
     * Мерж ячеек.
     *  @param firstRow начальная строка.
     * @param lastRow конечная строка.
     * @param firstColumn начальный столбец.
     * @param lastColumn конечный столбец.
     * @param sheet страница.
     * @param headerStyle стиль на регион.
     * @param column внутренний вид столбца.
     */
    protected abstract void mergeRegion(int firstRow, int lastRow, int firstColumn, int lastColumn, TS sheet, StyleImpl headerStyle, Column<?, Object> column);

    /**
     * Установить признак автосайза.
     *
     * @param sheetToWrite страница.
     * @param index индекс столбца.
     */
    protected abstract void setAutosize(TS sheetToWrite, int index);

    /**
     * Получение числа строк.
     *
     * @param sheetToWrite страница.
     * @return число строк.
     */
    protected abstract int getRows(TS sheetToWrite);
}
