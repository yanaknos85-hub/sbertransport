package ru.sber.transport.spreadsheet.fastexcel.abstraction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dhatim.fastexcel.Worksheet;

import java.util.List;

/**
 * Абстракция над двумя типами рабочих страниц в fastexcel.
 */
@Getter
@RequiredArgsConstructor
public class Sheet {

    /**
     * Рабочая книга.
     */
    private final Workbook workbook;

    /**
     * Оригинальная страница fastexcel для записи.
     */
    private final Worksheet writableSheet;

    /**
     * Название страницы.
     */
    private final String name;

    /**
     * Строки для чтения.
     */
    private final List<Row> readableRows;

    /**
     * Слитые ячейки для чтения.
     */
    private final List<CellRangeAddress> merged;
}
