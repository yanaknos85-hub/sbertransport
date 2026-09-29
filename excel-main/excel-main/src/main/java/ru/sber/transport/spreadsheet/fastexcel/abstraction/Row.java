package ru.sber.transport.spreadsheet.fastexcel.abstraction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Абстракция над двумя типами строк в fastexcel.
 */
@Getter
@RequiredArgsConstructor
public class Row {

    /**
     * Рабочий лист.
     */
    private final Sheet sheet;

    /**
     * Индекс строки на рабочем листе.
     */
    private final int index;

    /**
     * Ячейки строки для чтения.
     */
    private final Map<Integer, Cell> readableCells;

    /**
     * Получение количества ячеек для чтения.
     *
     * @return количество ячеек для чтения.
     */
    public int getReadableCellCount() {
        if (readableCells == null || readableCells.isEmpty()) {
            return 0;
        }

        return (Integer) readableCells.keySet().toArray()[readableCells.size() - 1] + 1;
    }

    /**
     * Получение ячейки для чтения.
     *
     * @param index индекс в строке.
     * @return ячейка.
     */
    public Cell getReadableCell(int index) {
        return readableCells.get(index);
    }
}
