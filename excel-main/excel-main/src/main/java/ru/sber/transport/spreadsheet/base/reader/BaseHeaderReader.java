package ru.sber.transport.spreadsheet.base.reader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * Базовый класс записи заголовков.
 *
 * @param <S> тип целевой страницы.
 */
@RequiredArgsConstructor
public abstract class BaseHeaderReader<S> {

    private final S workSheet;

    /**
     * Чтение заголовков.
     *
     * @return количество строк заголовков.
     */
    public int read() {
        var regions = getMergedRegions(workSheet);
        var headerRowIndex = 0;
        if (regions != null) {
            for (var region : regions) {
                if (region.containsRow(0)) {
                    var rowCount = region.getLastRow();
                    if (headerRowIndex < rowCount) {
                        headerRowIndex = rowCount;
                    }
                }
            }
        }
        return headerRowIndex + 1;
    }

    /**
     * Получение смерженных ячеек.
     *
     * @param workSheet рабочая страница.
     * @return смерженные ячейки.
     */
    protected abstract List<CellRangeAddress> getMergedRegions(S workSheet);
}
