package ru.sber.transport.spreadsheet.base.writer;

import lombok.RequiredArgsConstructor;
import ru.sber.transport.spreadsheet.excel.Writer;

import java.util.List;
import java.util.Objects;

/**
 * Базовый писатель заголовка.
 *
 * @param <TS> тип целевой страницы.
 * @param <R> тип строки.
 * @param <C> тип ячейки.
 */
@SuppressWarnings("java:S119")
@RequiredArgsConstructor
public abstract class BaseCaptionWriter<TS, R, C> implements Writer {

    private final TS targetSheet;

    private final List<List<String>> attention;

    @Override
    public int write() {
        return writeCaption(targetSheet, attention);
    }

    /**
     * Записать Верхний колонтитул.
     *
     * @param sheet страница для записи Верхнего колонтитула.
     * @param caption Верхний колонтитул.
     * @return количество добавленных строк.
     */
    private int writeCaption(TS sheet, List<List<String>> caption) {
        if (caption == null || caption.parallelStream().flatMap(List::parallelStream).filter(Objects::nonNull).toList().isEmpty()) {
            return 0;
        }

        for (var rowIndex = 0; rowIndex < caption.size(); rowIndex++) {
            final var row = getRow(sheet, rowIndex);
            for (var colIndex = 0; colIndex < caption.get(rowIndex).size(); colIndex++) {
                final var cell = getCell(row, colIndex);
                setValue(cell, caption.get(rowIndex).get(colIndex));
                postProcess(cell);
            }
        }

        return caption.size();
    }

    /**
     * Пост-процесс ячейки.
     *
     * @param cell ячейка.
     */
    protected void postProcess(C cell) {
    }

    /**
     * Установить значение ячейки.
     *
     * @param cell ячейка для установки значения.
     * @param caption строковое значение.
     */
    protected abstract void setValue(C cell, String caption);

    /**
     * Получить строку.
     *
     * @param sheet страница.
     * @param index индекс строки.
     * @return строка.
     */
    protected abstract R getRow(TS sheet, int index);

    /**
     * Получить ячейку.
     *
     * @param row строка.
     * @param index индекс ячейки.
     * @return ячейка.
     */
    protected abstract C getCell(R row, int index);

}
