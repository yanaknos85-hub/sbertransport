package ru.sber.transport.spreadsheet.base;

import ru.sber.transport.spreadsheet.excel.Column;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Базовая реализация контейнера столбцов.
 *
 * @param <T> тип данных.
 */
public abstract class BaseColumns<T> {
    
    /**
     * Список столбцов.
     */
    protected final List<Column<T, ?>> columns = new ArrayList<>();
    
    /**
     * Добавление столбца.
     *
     * @param name название столбца.
     * @param field поле объекта со значением столбца.
     * @param <V> тип данных.
     * @return столбец.
     */
    public  <V> Column<T, V> addColumn(String name, String field) {
        var names = name.split("\\|");
        var fields = field.split("\\.");
        
        String curName;
        String curField;
        
        String childName = null;
        String childField = null;
        
        if (names.length > 1) {
            curName = names[0];
            curField = fields[0];
            
            childName = name.replaceFirst(names[0] + "\\|", "");
            childField = field.replaceFirst(fields[0] + "\\.", "");
            
            if (childField.equals(curField)) {
                curField = null;
            }
        } else {
            curName = name;
            curField = field;
        }
        
        var column = Column.<T, V>builder().name(curName).field(curField).build();
        
        var columnOptional = columns.stream()
                                    .filter(check -> check.getName().equals(curName))
                                    .findFirst();
        if (columnOptional.isPresent()) {
            column = ReflectionUtils.cast(columnOptional.get());
        } else {
            columns.add(column);
        }
        if (childName != null) {
            column.addChild(childName, childField);
        }
        return column;
    }
    
    /**
     * Добавление столбца.
     *
     * @param name название столбца.
     * @param value функция выбора значения из объекта.
     * @param <V> тип значения.
     * @return столбец.
     */
    public  <V> Column<T, V> addColumn(String name, Function<T, V> value) {
        return addColumn(name, value, (Supplier<String>) null);
    }

    /**
     * Добавление столбца.
     *
     * @param name название столбца.
     * @param value функция выбора значения из объекта.
     * @param nullRenderer функция отображения отсутствующего значения.
     * @param <V> тип значения.
     * @return столбец.
     */
    public  <V> Column<T, V> addColumn(String name, Function<T, V> value, Supplier<String> nullRenderer) {
        var column = Column.<T, V>builder().name(name).valueExtractor(value).nullRender(nullRenderer).build();
        columns.add(column);
        return column;
    }
    
    /**
     * Добавление столбца.
     *
     * @param name название столбца.
     * @param value функция выбора значения из объекта.
     * @param toPrimitive преобразование значения в примитив.
     * @param <V> тип значения.
     * @return столбец.
     */
    public <V> Column<T, V> addColumn(String name, Function<T, V> value, Function<V, Object> toPrimitive) {
        return addColumn(name, value, toPrimitive, null);
    }

    /**
     * Добавление столбца.
     *
     * @param name название столбца.
     * @param value функция выбора значения из объекта.
     * @param toPrimitive преобразование значения в примитив.
     * @param nullRenderer функция отображения отсутствующего значения.
     * @param <V> тип значения.
     * @return столбец.
     */
    public <V> Column<T, V> addColumn(String name, Function<T, V> value, Function<V, Object> toPrimitive, Supplier<String> nullRenderer) {
        var column = Column.<T, V>builder().name(name).valueExtractor(value).toPrimitive(toPrimitive).nullRender(nullRenderer).build();
        columns.add(column);
        return column;
    }

    /**
     * Получить список столбцов.
     *
     * @return список столбцов.
     */
    public List<Column<T, Object>> getColumns() {
        return ReflectionUtils.cast(this.columns);
    }
}
