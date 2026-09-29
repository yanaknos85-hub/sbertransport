package ru.sber.transport.spreadsheet.base.reader;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.util.CellRangeAddress;
import ru.sber.transport.spreadsheet.base.reader.exception.HeaderValidationException;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Базовый читатель данных.
 *
 * @param <T> тип данных.
 * @param <S> тип страницы.
 * @param <R> тип строки.
 * @param <C> тип столбца.
 */
@RequiredArgsConstructor
public abstract class BaseDataReader<T, S, R, C> {

    private final S workSheet;

    private final Class<T> valueClass;

    private final List<Column<T, Object>> columns;

    private int headersCount;

    /**
     * Установить номер строки для старта чтения.
     *
     * @param startRowNumber номер строки.
     */
    public void setStartRow(int startRowNumber) {
        this.headersCount = startRowNumber;
    }

    /**
     * Получить итератор по данным.
     *
     * @param ignoreFormulas игнорирование формул в ячейках.
     * @param validator      валидатор входящих данных.
     * @param checkHeadMatch флаг проверки правильноста заголовка.
     * @return итератор для чтения данных.
     */
    public Iterator<T> iterator(boolean ignoreFormulas, Validator validator, boolean checkHeadMatch) {
        var columnsMap = extractItems(ReflectionUtils.cast(columns), false);
        var columnMapping = extractMappings(columns);
        var worksheetIndices = extractIndices(workSheet, 0, new AtomicInteger(), null);
        var columnsIndices = mergeMappings(columnMapping, worksheetIndices);

        validateHead(columnsIndices, columnMapping, checkHeadMatch);

        var keyValueColumns = columnMapping
            .entrySet()
            .parallelStream()
            .filter(e -> e.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().parallelStream().map(v -> {
                var parts = v.split("\\|");
                return parts[parts.length - 1];
            }).toList()));

        return createDataIterator(headersCount, valueClass, columnsIndices, columnsMap, keyValueColumns, workSheet, validator, ignoreFormulas);
    }

    private void validateHead(Map<String, List<Integer>> columnsIndices, Map<String, List<String>> columnMapping, boolean checkHeadMatch) {
        if (!checkHeadMatch) {
            return;
        }

        var brokenColumns = columnsIndices
                .entrySet()
                .parallelStream()
                .filter(c -> c.getValue().isEmpty())
                .map(c -> columnMapping.get(c.getKey()).get(0))
                .toList();

        if (!brokenColumns.isEmpty()) {
            throw new HeaderValidationException(brokenColumns);
        }
    }

    /**
     * Объединение маппингов.
     *
     * @param columnMapping    маппинг распознанных столбцов.
     * @param worksheetIndices маппинг индексов столбцов рабочей страницы.
     * @return маппинг индексов и распознанных столбцов.
     */
    private Map<String, List<Integer>> mergeMappings(Map<String, List<String>> columnMapping, Map<String, Integer> worksheetIndices) {
        var result = new LinkedHashMap<String, List<Integer>>();
        for (var mappingEntry : columnMapping.entrySet()) {
            var index = worksheetIndices.get(mappingEntry.getValue().get(0));
            result.put(mappingEntry.getKey(), index != null ? List.of(index) : null);
        }
        result.entrySet().parallelStream().filter(e -> e.getValue() == null).forEach(entry -> {
            var fieldName = columnMapping.get(entry.getKey());
            var indices = new LinkedList<Integer>();
            var names = new LinkedList<String>();
            for (var workSheetEntry : worksheetIndices.entrySet().parallelStream().filter(e -> e.getKey().startsWith(fieldName.get(0) + "|")).toList()) {
                indices.add(workSheetEntry.getValue());
                names.add(workSheetEntry.getKey());
            }
            entry.setValue(indices);
            if (!names.isEmpty()) {
                columnMapping.put(entry.getKey(), names);
            }
        });
        return result;
    }

    /**
     * Получение индексов столбцов.
     *
     * @param workSheet        рабочая страница.
     * @param startRowIndex    инлекс первой строки с данными.
     * @param startColumnIndex начальный индекс столбца.
     * @param children         дочерние столбцы.
     * @return индексы.
     */
    private Map<String, Integer> extractIndices(S workSheet, int startRowIndex,
                                                AtomicInteger startColumnIndex, Integer children) {
        var indicesResult = new LinkedHashMap<String, Integer>();
        var row = getRow(workSheet, startRowIndex);
        var nextRow = getRow(workSheet, startRowIndex + 1);
        var regions = getMerged(workSheet);

        var cells = children == null ? getLastCell(row) : startColumnIndex.get() + children;
        for (; startColumnIndex.get() < cells; startColumnIndex.incrementAndGet()) {
            var cell = getCell(row, startColumnIndex.get());
            var nextCell = Optional.ofNullable(nextRow).map(r -> getCell(r, startColumnIndex.get())).orElse(null);
            if (cell == null) {
                continue;
            }
            processCell(workSheet, startRowIndex, startColumnIndex, indicesResult, regions, cell, nextCell);
        }
        return indicesResult;
    }

    /**
     * Получение маппинга столбцов и полей.
     *
     * @param source коллекция столбцов.
     * @return маппинг.
     */
    private LinkedHashMap<String, List<String>> extractMappings(List<? extends Column<?, Object>> source) {
        var mapping = new LinkedHashMap<String, List<String>>();
        for (var column : source) {
            var field = column.getField();
            var name = column.getName();

            var children = column.getChildren();
            if (children.isEmpty()) {
                append(mapping, field, name);
            } else {
                var childrenMapping = extractMappings(children);
                for (var entry : childrenMapping.entrySet()) {
                    processChild(mapping, field, name, entry);
                }
            }
        }
        return mapping;
    }

    /**
     * Получение плоской мапы столбцов.
     *
     * @param source         столбцы.
     * @param includeParents отображать родительские подразделения.
     * @return столбцы.
     */
    private LinkedHashMap<String, Column<?, ?>> extractItems(List<Column<Object, Object>> source, boolean includeParents) {
        return source.stream().map(c -> extractColumn(includeParents, c))
            .reduce((l, r) -> {
                l.putAll(r);
                return l;
            })
            .orElseGet(LinkedHashMap::new);
    }

    private LinkedHashMap<String, Column<?, ?>> extractColumn(boolean includeParents, Column<Object, Object> column) {
        var result = new LinkedHashMap<String, Column<?, ?>>();
        var children = column.getChildren();
        if (!children.isEmpty()) {
            var field = column.getField();
            if (includeParents) {
                result.put(field, column);
            }
            var childrenMap = extractItems(children, includeParents);
            for (var entry : childrenMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (field != null && !field.equals("null")) {
                    result.put(field + "." + key, value);
                } else {
                    result.put(key, value);
                }
            }
        } else {
            result.put(column.getField(), column);
        }
        return result;
    }

    private void processChild(Map<String, List<String>> mapping, String field, String name, Map.Entry<String, List<String>> entry) {
        for (var entryItem : entry.getValue()) {
            if (field != null && !field.equals("null")) {
                append(mapping, field + "." + entry.getKey(), name + "|" + entryItem);
            } else {
                append(mapping, entry.getKey(), name + "|" + entryItem);
            }
        }
    }

    private void append(Map<String, List<String>> mapping, String field, String name) {
        mapping.compute(field, (f, l) -> {
            if (l == null) {
                l = new LinkedList<>();
            }
            l.add(name);
            return l;
        });
    }

    private void processCell(S workSheet, int startRowIndex, AtomicInteger startColumnIndex, Map<String, Integer> indicesResult, List<CellRangeAddress> merges, C cell, C nextCell) {
        var cellName = getString(cell);
        if (!cellName.isBlank()) {
            if (nextCell != null && isNotBlank(nextCell) && startRowIndex < headersCount - 1) {
                var merged = merges.parallelStream()
                                 .filter(region -> region.containsRow(startRowIndex))
                                 .filter(region -> region.containsColumn(startColumnIndex.get()))
                                 .filter(region -> region.getFirstRow() == region.getLastRow())
                                 .mapToInt(region -> region.getLastColumn() - region.getFirstColumn()).findFirst().orElse(0) + 1;
                var indices = extractIndices(workSheet, startRowIndex + 1, startColumnIndex, merged);
                if (startRowIndex < headersCount) {
                    startColumnIndex.getAndDecrement();
                }
                for (var entry : indices.entrySet()) {
                    indicesResult.put(cellName + "|" + entry.getKey(), entry.getValue());
                }
            } else {
                indicesResult.put(cellName, startColumnIndex.get());
            }
        }
    }

    /**
     * Создать новый итератор данных.
     *
     * @param headersCount количество строк-заголовков.
     * @param valueClass класс значения.
     * @param columnsIndices индексы столбцов.
     * @param columnsMap сопоставление столбцов и полей.
     * @param keyValueColumns столбцы, представляющие собой мапы ключ-значение.
     * @param workSheet рабочая страница.
     * @param validator валидатор.
     * @param ignoreFormulas флаг игнорирования формул.
     */
    @SuppressWarnings("java:S107")
    protected abstract BaseDataIterator<T, S, R, C> createDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?,?>> columnsMap, Map<String, List<String>> keyValueColumns, S workSheet, Validator validator, boolean ignoreFormulas);

    /**
     * Получение строки.
     *
     * @param workSheet рабочая страница.
     * @param index индекс строки.
     * @return строка.
     */
    protected abstract R getRow(S workSheet, int index);

    /**
     * Получение смерженных ячеек.
     *
     * @param workSheet рабочая страница.
     * @return смерженные ячейки.
     */
    protected abstract List<CellRangeAddress> getMerged(S workSheet);

    /**
     * Получение номера последней ячейки.
     *
     * @param row строка.
     * @return номер последней ячейки.
     */
    protected abstract int getLastCell(R row);

    /**
     * Получение ячейки.
     *
     * @param row строка.
     * @param index номер ячейки.
     * @return ячейка.
     */
    protected abstract C getCell(R row, int index);

    /**
     * Получение строкового значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract String getString(C cell);

    /**
     * Проверка пустоты ячейки.
     *
     * @param cell ячейка.
     * @return флаг пустоты.
     */
    protected abstract boolean isNotBlank(C cell);

}
