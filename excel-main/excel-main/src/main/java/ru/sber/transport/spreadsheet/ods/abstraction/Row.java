package ru.sber.transport.spreadsheet.ods.abstraction;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;

/**
 * Абстракция строки для формата ODS.
 *
 * @param index индекс строки.
 * @param sheet рабочая страница.
 */
public record Row(Sheet sheet, int index) {

    public Row {
        if (sheet.getMaxRows() - 1 < index) {
            sheet.appendRows(index - sheet.getMaxRows() + 1);
        }
    }

    /**
     * Получение данных ячейки.
     *
     * @param index индекс ячейки.
     * @return массив данных.
     */
    Range getRange(int index) {
        return sheet.getRange(this.index, index);
    }

    /**
     * Получение флага наличия формулы.
     *
     * @param index индекс ячейки.
     * @return флаг наличия формулы.
     */
    boolean hasFormula(int index) {
        var formula = getRange(index).getFormula();
        return formula != null && !formula.isBlank();
    }
}
