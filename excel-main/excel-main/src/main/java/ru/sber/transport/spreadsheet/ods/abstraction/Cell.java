package ru.sber.transport.spreadsheet.ods.abstraction;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Абстракция ячейки для работы с библиотекой ODS.
 */
@Getter
@Accessors(fluent = true)
public final class Cell {

    private final Row row;

    private final int index;

    private Object value;

    /**
     * Создать новую ячейку.
     *
     * @param row данные строки.
     * @param index индекс ячейки.
     */
    public Cell(
        Row row,
        int index
    ) {
        this.row = row;
        this.index = index;
        var sheet = row.sheet();
        if (sheet.getMaxColumns() - 1 < index) {
            sheet.appendColumns(index - sheet.getMaxColumns() + 1);
        }
        var range = sheet.getRange(row.index(), index);
        value = range.getValue();
        if (range.isPartOfMerge()) {
            var merged = range.getMergedCells()[0];
            if (row.index() > merged.getRow()) {
                value = null;
            }
        }
    }

    /**
     * Установить значение ячейки.
     *
     * @param newValue новое значение ячейки.
     */
    public void value(Object newValue) {
        value = newValue;
        row.getRange(index).setValue(newValue);
    }

    /**
     * Получение флага наличия формулы.
     *
     * @return флаг наличия формулы.
     */
    public boolean hasFormula() {
        return row.hasFormula(index);
    }

}
