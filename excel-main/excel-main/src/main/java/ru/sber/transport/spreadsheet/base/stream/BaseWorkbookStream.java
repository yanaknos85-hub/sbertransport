package ru.sber.transport.spreadsheet.base.stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.sber.transport.spreadsheet.base.BaseSheet;
import ru.sber.transport.spreadsheet.base.SpreadSheet;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.base.writer.BaseCaptionWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseDataWriter;
import ru.sber.transport.spreadsheet.base.writer.BaseHeaderWriter;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Базовый стрим книги.
 *
 * @param <SW> тип исходной книги.
 * @param <SS> тип исходной страницы.
 * @param <TW> тип целевой книги.
 * @param <TS> тип целевой страницы.
 * @param <R> тип строки.
 * @param <C> тип ячейки.
 */
@SuppressWarnings("java:S119")
@RequiredArgsConstructor
public abstract class BaseWorkbookStream<SW extends SpreadSheet<SW, SS, TW, TS, R, C>, SS extends BaseSheet<Object, SW, TS, R, C>, TW, TS, R, C> implements AutoCloseable {

    private final SW spreadSheet;

    private final OutputStream stream;

    private final TW workbookToWrite;

    /**
     * Записать данные.
     */
    public void write() {
        getSheets(spreadSheet).forEach(this::writeSheet);
    }

    /**
     * Записать отдельную страницу.
     *
     * @param sheet страница для записи в файл.
     */
    public void writeSheet(@NonNull SS sheet) {
        try (var dataWriter = startWrite(sheet)) {
            for (var item : sheet.getData()) {
                dataWriter.writeNext(item);
            }
        }
    }

    /**
     * Подготовка страницы книги к записи.
     *
     * @param sheet страница для записи.
     * @return помощник по записи данных.
     */
    public BaseDataWriter<Object, SS, TS, R, C> startWrite(@NonNull SS sheet) {
        var sheetToWrite = getSheet(sheet, workbookToWrite);
        demapValueColumns(sheet);
        var attentionWriter = createCaptionWriter(sheetToWrite, sheet.getCaption());
        try (var headerWriter = createHeaderWriter(sheetToWrite, sheet.getColumns(), sheet.getHeaderStyle())) {
            headerWriter.setStartRow(attentionWriter.write());
            var headerRows = headerWriter.write();
            return createDataWriter(sheetToWrite, sheet, headerRows);
        }
    }

    @Override
    public void close() throws IOException {
        close(workbookToWrite, stream);
    }

    @SuppressWarnings("java:S4276")
    private void demapValueColumns(SS sheet) {
        if (!sheet.getData().isEmpty()) {
            var columns = new ArrayList<>(sheet.getColumns());
            columns.addAll(sheet.getColumns().parallelStream().flatMap(c -> c.getChildren().parallelStream()).toList());
            columns.parallelStream()
                .filter(column -> {
                    var value = extractValue(column, sheet.getData().get(0));
                    return value instanceof Map<?, ?>;
                })
                .forEach(column -> {
                    for (var key : extractKeys(sheet, column)) {
                        var field = column.getField();
                        var extractor = column.getValueExtractor();
                        if (field != null) {
                            column.addColumn(key, "%s.%s".formatted(field, key));
                        }
                        if (extractor != null) {
                            column.addColumn(key, o -> ReflectionUtils.castObjectToMap(o).get(key));
                        }
                    }
            });
        }
    }

    private List<String> extractKeys(SS sheet, Column<Object, Object> column) {
        return sheet.getData().parallelStream()
            .filter(Objects::nonNull)
            .map(o -> extractValue(column, o))
            .map(m -> ReflectionUtils.castObjectToMap(m, String.class, Object.class))
            .filter(Objects::nonNull)
            .map(Map::keySet)
            .flatMap(Collection::parallelStream)
            .sorted()
            .distinct()
            .toList();
    }

    @SuppressWarnings("java:S4276")
    private Object extractValue(Column<Object, Object> column, Object object) {
        var extractor = column.getValueExtractor();
        if (extractor != null) {
            return extractor.apply(object);
        }
        return extractValue(column.getFullFieldName(), object);
    }

    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class})
    private Object extractValue(String fieldName, Object object) {
        if (fieldName != null) {
            var objectClass = object.getClass();
            Field field;
            if (fieldName.contains(".")) {
                var fieldParts = fieldName.split("\\.");
                var subField = ReflectionUtils.getField(objectClass, fieldParts[0]);
                subField.trySetAccessible();
                var subItem = subField.get(object);
                field = ReflectionUtils.getField(subItem.getClass(), fieldParts[1]);
                field.trySetAccessible();
                return field.get(subItem);
            } else {
                field = ReflectionUtils.getField(objectClass, fieldName);
                field.trySetAccessible();
                return field.get(object);
            }
        }
        return null;
    }

    /**
     * Закрыть поток.
     *
     * @param workbookToWrite книга для записи.
     * @param stream целевой поток.
     * @throws IOException ошибка ввода/вывода.
     */
    protected abstract void close(TW workbookToWrite, OutputStream stream) throws IOException;

    /**
     * Получить список страниц.
     *
     * @param source исходная книга.
     * @return коллекция страниц.
     */
    protected abstract Collection<SS> getSheets(SW source);

    /**
     * Получить страницу.
     *
     * @param sheet исходная страница.
     * @param workbook целевая книга.
     * @return целевая страница.
     */
    protected abstract TS getSheet(SS sheet, TW workbook);

    /**
     * Создать писатель аннотации.
     *
     * @param sheetToWrite целевая страница.
     * @param caption аннотация.
     * @return писатель.
     */
    protected abstract BaseCaptionWriter<TS, R, C> createCaptionWriter(TS sheetToWrite, List<List<String>> caption);

    /**
     * Создать писатель заголовков.
     *
     * @param sheetToWrite целевая страница.
     * @param columns столбцы.
     * @param headerStyle стиль.
     * @return писатель.
     */
    protected abstract BaseHeaderWriter<TS, R, C> createHeaderWriter(TS sheetToWrite, List<? extends Column<?, Object>> columns, StyleImpl headerStyle);

    /**
     * Создать писатель данных.
     *
     * @param sheetToWrite целевая страница.
     * @param sheet страница.
     * @param headerRows количество строк головы.
     * @return писатель.
     */
    @SuppressWarnings("java:S1452")
    protected abstract BaseDataWriter<Object, SS, TS, R, C> createDataWriter(TS sheetToWrite, SS sheet, int headerRows);

}
