package ru.sber.transport.spreadsheet.excel;

import lombok.*;
import ru.sber.transport.spreadsheet.base.BaseColumns;
import ru.sber.transport.spreadsheet.base.StyleImpl;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Метаданные столбца.
 *
 * @param <T> тип данных.
 * @param <V> тип примитива, отображающего данные.
 */
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
public class Column<T, V> extends BaseColumns<V> {
    
    /**
     * Название столбца.
     */
    @ToString.Include
    private final String name;
    
    /**
     * Поле, соотносящиеся со столбцом.
     */
    @ToString.Include
    private final String field;
    
    /**
     * Стиль столбца.
     */
    @Getter
    private final StyleImpl style = new StyleImpl();
    
    /**
     * Функция получения значения.
     */
    private final Function<T, V> valueExtractor;
    
    /**
     * To primitive converter. It needs for converting any object to one of Excel primitives (String, Number, LocalDate,
     * LocalDateTime, Calendar, Date or Boolean).
     */
    private final Function<V, Object> toPrimitive;

    /**
     * Функция, отображающая отсутствующее значение.
     */
    private final Supplier<String> nullRender;

    /**
     * Родительский столбец.
     */
    @Getter
    private Column<?, ?> parent;

    /**
     * Добавление дочернего (вложенного) столбца.
     *
     * @param name название столбца.
     * @param field название поля для маппинга на объект.
     * @return метаданные столбца.
     * @param <V1> тип значения.
     */
    public <V1> Column<V, V1> addChild(String name, String field) {
        var child = super.<V1>addColumn(name, field);
        child.parent = this;
        return child;
    }

    /**
     * Добавление дочернего (вложенного) столбца.
     *
     * @param name название столбца.
     * @param value функция получения значения.
     * @return метаданные столбца.
     * @param <V1> тип значения.
     */
    public <V1> Column<V, V1> addChild(String name, Function<V, V1> value) {
        var child = super.addColumn(name, value, (Supplier<String>) null);
        child.parent = this;
        return child;
    }

    /**
     * Добавление дочернего (вложенного) столбца.
     *
     * @param name название столбца.
     * @param value функция получения значения.
     * @param nullRender рендерер пустых значений.
     * @return метаданные столбца.
     * @param <V1> тип значения.
     */
    public <V1> Column<V, V1> addChild(String name, Function<V, V1> value, Supplier<String> nullRender) {
        var child = super.addColumn(name, value, nullRender);
        child.parent = this;
        return child;
    }

    /**
     * Добавление дочернего (вложенного) столбца.
     *
     * @param name название столбца.
     * @param value функция получения значения.
     * @param toPrimitive функция приведения к примитиву.
     * @return метаданные столбца.
     * @param <V1> тип значения.
     */
    public <V1> Column<V, V1> addChild(
            String name, Function<V, V1> value, Function<V1, Object> toPrimitive
                                       ) {
        var child = super.addColumn(name, value, toPrimitive, null);
        child.parent = this;
        return child;
    }

    /**
     * Добавление дочернего (вложенного) столбца.
     *
     * @param name название столбца.
     * @param value функция получения значения.
     * @param toPrimitive функция приведения к примитиву.
     * @param nullRenderer функция отображения отсутствующего значения.
     * @return метаданные столбца.
     * @param <V1> тип значения.
     */
    public <V1> Column<V, V1> addChild(
            String name, Function<V, V1> value, Function<V1, Object> toPrimitive, Supplier<String> nullRenderer
                                       ) {
        var child = super.addColumn(name, value, toPrimitive, nullRenderer);
        child.parent = this;
        return child;
    }

    /**
     * Получение дочерних (вложенных) столбцов.
     *
     * @return столбцы.
     */
    public List<Column<V, Object>> getChildren() {
        return super.getColumns();
    }

    /**
     * Получение полного имени поля.
     *
     * @return полное имя поля.
     */
    public String getFullFieldName() {
        if (parent == null) {
            return field;
        }
        return "%s.%s".formatted(parent.getFullFieldName(), field);
    }
}
