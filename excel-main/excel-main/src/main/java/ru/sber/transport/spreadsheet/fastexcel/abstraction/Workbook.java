package ru.sber.transport.spreadsheet.fastexcel.abstraction;

import lombok.Getter;
import lombok.SneakyThrows;
import org.dhatim.fastexcel.reader.ReadableExtendedWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Абстракция над двумя типами рабочих книг в fastexcel.
 */
@Getter
public class Workbook {

    /**
     * Оригинальная рабочая книга для чтения.
     */
    private ReadableExtendedWorkbook readableWorkbook;

    /**
     * Оригинальная рабочая книга для записи.
     */
    private org.dhatim.fastexcel.Workbook writableWorkbook;

    /**
     * Создать рабочую книгу для чтения.
     *
     * @param stream стрим данных для чтения.
     */
    public Workbook(InputStream stream) throws IOException {
        readableWorkbook = new ReadableExtendedWorkbook(stream);
        readableWorkbook.getPkg();
    }

    /**
     * Создать рабочую книгу для записи.
     *
     * @param stream стрим данных для записи.
     */
    public Workbook(OutputStream stream) {
        writableWorkbook = new org.dhatim.fastexcel.Workbook(stream, "SberTransport", "1.0");
    }

    /**
     * Получение страницы.
     *
     * @param name название.
     * @param read флаг для чтения или записи.
     * @return страница.
     */
    @SneakyThrows(IOException.class)
    public Sheet getSheet(String name, boolean read) {
        if (read) {
            var originalSheet = readableWorkbook.findSheet(name).orElseThrow();
            return getSheet(originalSheet);
        } else {
            var sheet = writableWorkbook.newWorksheet(name);
            return new Sheet(this, sheet, name, null, null);
        }
    }

    /**
     * Получение страницы для чтения.
     *
     * @param idx индекс.
     * @return страница.
     */
    @SneakyThrows(IOException.class)
    public Sheet getSheet(int idx) {
        var originalSheet = readableWorkbook.getSheet(idx).orElseThrow();

        return getSheet(originalSheet);
    }

    /**
     * Сохранить записываемый файл.
     */
    public void save() throws IOException {
        if (writableWorkbook != null) {
            writableWorkbook.finish();
        }
    }

    /**
     * Закрыть открытые рабочие книги.
     */
    public void close() throws IOException {
        if (readableWorkbook != null) {
            readableWorkbook.close();
            readableWorkbook = null;
        }

        if (writableWorkbook != null) {
            writableWorkbook.close();
            writableWorkbook = null;
        }
    }

    private Sheet getSheet(org.dhatim.fastexcel.reader.Sheet readableSheet) throws IOException {
        var rows = new LinkedList<Row>();
        var merged = readableWorkbook.openMergeStream(readableSheet).toList();
        var sheet = new Sheet(this, null, readableSheet.getName(), rows, merged);

        rows.addAll(readableSheet.read().stream().map(row -> {
            var c = new HashMap<Integer, Cell>();
            var r = new Row(sheet, row.getRowNum() - 1, c);
            row.getCells(0, row.getCellCount())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(cell -> new Cell(r, cell.getValue(), cell.getRawValue(), cell.getType(), cell.getColumnIndex()))
                    .forEach(cell -> c.put(cell.getIndex(), cell));
            return r;
        }).toList());

        return sheet;
    }
}
