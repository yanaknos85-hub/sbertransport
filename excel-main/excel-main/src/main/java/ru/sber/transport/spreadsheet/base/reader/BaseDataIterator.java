package ru.sber.transport.spreadsheet.base.reader;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dhatim.fastexcel.reader.ExcelReaderException;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.annotation.ValueType;
import ru.sber.transport.spreadsheet.base.reader.exception.DateParsingViolation;
import ru.sber.transport.spreadsheet.base.reader.exception.NumberParsingViolation;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.model.ValidationError;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQuery;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Базовый итератор по рабочей книге.
 *
 * @param <T> тип данных.
 * @param <S> тип страницы книги.
 * @param <R> тип строки.
 * @param <C> тип ячейки.
 */
@Slf4j
public abstract class BaseDataIterator<T, S, R, C> implements Iterator<T> {

    private final Class<T> valueClass;

    private final Map<String, List<Integer>> columnsIndices;

    private final Map<String, Column<?, ?>> columnsMap;

    private final Map<String, List<String>> keyValueColumns;

    private final S workSheet;

    private final Validator validator;

    private final boolean ignoreFormulas;

    private int currentRow;

    /**
     * Создать новый итератор.
     *
     * @param headersCount    количество строк-заголовков.
     * @param valueClass      класс значения.
     * @param columnsIndices  индексы столбцов.
     * @param columnsMap      сопоставление столбцов и полей.
     * @param keyValueColumns столбцы, представляющие собой карты ключ-значение.
     * @param workSheet       рабочая страница.
     * @param validator       валидатор.
     * @param ignoreFormulas  флаг игнорирования формул.
     */
    protected BaseDataIterator(int headersCount, Class<T> valueClass, Map<String, List<Integer>> columnsIndices, Map<String, Column<?, ?>> columnsMap, Map<String, List<String>> keyValueColumns, S workSheet, Validator validator, boolean ignoreFormulas) {
        this.valueClass = valueClass;
        this.columnsIndices = columnsIndices;
        this.columnsMap = columnsMap;
        this.workSheet = workSheet;
        this.validator = validator;
        this.ignoreFormulas = ignoreFormulas;
        this.keyValueColumns = keyValueColumns;
        currentRow = headersCount - 1;
    }

    @Override
    public boolean hasNext() {
        return currentRow < getRowsCount(workSheet);
    }

    @Override
    public T next() {
        var rowIndex = ++currentRow;
        var row = getRow(workSheet, rowIndex);
        if (row == null) {
            return null;
        }
        var constructor = getConstructor(valueClass);
        prepareWorksheet(workSheet);
        return parseRow(row, constructor);
    }

    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class, InstantiationException.class,
            InvocationTargetException.class})
    private T parseRow(R row,
                       Constructor<T> constructor) {
        if (containsValue(row, columnsIndices.size())) {
            var dataItem = readRow(row, constructor);
            if (validator != null) {
                var violationSet = validator.validate(dataItem);
                if (!violationSet.isEmpty()) {
                    throw new ValidationError(getRowNumber(row), defineColumnsMap(violationSet));
                }
            }
            if (dataItem != null) {
                return ReflectionUtils.cast(dataItem);
            }
        }
        return null;
    }

    private Map<Column<?, ?>, Set<ConstraintViolation<?>>> defineColumnsMap(Set<ConstraintViolation<Object>> violationSet) {
        return violationSet.parallelStream()
                .map(v -> new AbstractMap.SimpleEntry<Column<?, ?>, ConstraintViolation<?>>(columnsMap.get(v.getPropertyPath().toString()), v))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> Set.of(e.getValue()),
                        (l, r) -> Stream.concat(l.stream(), r.stream()).collect(Collectors.toUnmodifiableSet())));
    }

    private Object readRow(R row, Constructor<?> constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object dataItem = null;
        var keyIterator = columnsMap.keySet().iterator();
        var errors = new HashMap<Column<?, ?>, Set<ConstraintViolation<?>>>();

        for (var cellIndex = 0; cellIndex < getLastCellNumber(row); cellIndex++) {
            if (keyIterator.hasNext() && columnsIndices != null) {
                var key = keyIterator.next();

                var index = columnsIndices.get(key);
                if (index != null) {
                    var column = columnsMap.get(key);
                    try {
                        dataItem = appendValue(row, constructor, dataItem, key, index, column.getNullRender());
                    } catch (ConstraintViolationException e) {
                        errors.put(column, e.getConstraintViolations());
                    }
                } else {
                    log.warn("There is no index for field %s".formatted(key));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationError(getRowNumber(row), errors);
        }
        return dataItem;
    }

    private Object appendValue(R row, Constructor<?> constructor, Object dataItem, String key, List<Integer> index, Supplier<String> functionalNullRenderer) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (var i : index) {
            var keys = keyValueColumns.getOrDefault(key, List.of());
            String keyOfValue = null;
            if (keys.size() > 1) {
                var keyIndex = columnsIndices.get(key).indexOf(i);
                keyOfValue = keys.get(keyIndex);
            }
            var cell = getCell(row, i);
            if (cell != null) {
                if (dataItem == null) {
                    dataItem = constructor.newInstance();
                }
                setFieldValue(dataItem, key, cell, keyOfValue, functionalNullRenderer);
            }
        }
        return dataItem;
    }

    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class, InstantiationException.class,
            InvocationTargetException.class})
    private void setFieldValue(Object dataItem, String fieldName, C cell, String keyOfValue, Supplier<String> functionalNullRenderer) {
        fieldName = fieldName.replaceFirst("null\\.", "");
        var fieldPaths = fieldName.split("\\.");
        var curFieldName = fieldPaths[0];
        var field = ReflectionUtils.getField(dataItem.getClass(), curFieldName);
        if (field == null) {
            return;
        }
        Object value = null;
        var fieldType = field.getType();
        if (!field.canAccess(dataItem)) {
            field.setAccessible(true); // NOSONAR reflective operation required.
            value = field.get(dataItem);
        }
        if (fieldPaths.length > 1) {
            value = setFieldValue(dataItem, fieldName, cell, keyOfValue, functionalNullRenderer, curFieldName, field, fieldType);
        } else if (cell != null) {
            if (Map.class.isAssignableFrom(fieldType)) {
                if (value == null) {
                    value = new HashMap<>();
                }
                var definedValue = ReflectionUtils.castObjectToMap(defineValue(fieldName, cell, keyOfValue, functionalNullRenderer, field, fieldType), String.class, Object.class);
                ((Map<String, Object>) value).putAll(definedValue);
            } else {
                value = defineValue(fieldName, cell, keyOfValue, functionalNullRenderer, field, fieldType);
            }
        }
        field.set(dataItem, ReflectionUtils.cast(value, fieldType)); // NOSONAR reflective operation required.
    }

    private Object setFieldValue(Object dataItem, String fieldName, C cell, String keyOfValue, Supplier<String> functionalNullRenderer, String curFieldName, Field field, Class<?> fieldType) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object value;
        var constructor = ReflectionUtils.getConstructor(fieldType);
        var item = field.get(dataItem);
        if (item == null) {
            item = getDefaultValue(fieldType, constructor);
        }
        setFieldValue(item, fieldName.replace(curFieldName + ".", ""), cell, keyOfValue, functionalNullRenderer);
        value = item;
        return value;
    }

    private Object getDefaultValue(Class<?> fieldType, Constructor<?> constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object item;
        if (constructor != null) {
            item = constructor.newInstance();
        } else if (LocalDateTime.class.isAssignableFrom(fieldType)) {
            item = LocalDateTime.now();
        } else if (LocalDate.class.isAssignableFrom(fieldType)) {
            item = LocalDate.now();
        } else {
            throw new NoSuchElementException();
        }
        return item;
    }

    private Object defineValue(String fieldName, C cell, String keyOfValue, Supplier<String> functionalNullRenderer, Field field, Class<?> fieldType) {
        try {
            var valueTypeAnn = getValueTypeAnnotation(field);
            Class<?> valueType = null;
            if (valueTypeAnn != null) {
                valueType = valueTypeAnn.value();
            }
            return defineValue(cell, fieldType, keyOfValue, valueType, field.getAnnotation(NullRender.class), field.getAnnotation(TimeFormat.class), functionalNullRenderer);
        } catch (DateTimeParseException e) {
            if (validator != null) {
                throw new ConstraintViolationException(Set.of(new DateParsingViolation(fieldName, fieldType, getString(cell))));
            }
        } catch (NumberFormatException | ExcelReaderException e) {
            if (validator != null) {
                throw new ConstraintViolationException(Set.of(new NumberParsingViolation(fieldName, getString(cell))));
            }
        }
        return null;
    }

    private ValueType getValueTypeAnnotation(Field field) {
        return ReflectionUtils.cast(Stream.concat(Arrays.stream(field.getAnnotations()), Arrays.stream(field.getDeclaredAnnotations()))
                .filter(a -> ValueType.class.isAssignableFrom(a.annotationType()))
                .findFirst()
                .orElse(null), ValueType.class);
    }

    private Object defineValue(C cell, Class<?> fieldType, String keyOfValue, Class<?> valueType, NullRender nullRender, TimeFormat timeFormat, Supplier<String> functionalNullRenderer) { // NOSONAR defining data type
        if (nullRender != null && nullRender.value().equalsIgnoreCase(getString(cell))) {
            return null;
        }
        if (functionalNullRenderer != null && functionalNullRenderer.get().equalsIgnoreCase(getString(cell))) {
            return null;
        }
        if (String.class.isAssignableFrom(fieldType)) {
            if (isNumber(cell)) {
                return getLong(cell);
            } else {
                return getString(cell);
            }
        } else if (Integer.class.isAssignableFrom(fieldType) || fieldType.getName().equals("int")) { // NOSONAR check primitive
            return getInteger(cell);
        } else if (Double.class.isAssignableFrom(fieldType) || fieldType.getName().equals("double")) { // NOSONAR check primitive
            return getDouble(cell);
        } else if (LocalDate.class.isAssignableFrom(fieldType)) {
            return getLocalDate(cell, nullRender, timeFormat);
        } else if (LocalDateTime.class.isAssignableFrom(fieldType)) {
            return getLocalDateTime(cell, nullRender, timeFormat);
        } else if (LocalTime.class.isAssignableFrom(fieldType)) {
            return getLocalTime(cell, nullRender, timeFormat);
        } else if (Boolean.class.isAssignableFrom(fieldType) || fieldType.getName().equals("boolean")) { // NOSONAR check primitive
            return getBoolean(cell);
        } else if (Long.class.isAssignableFrom(fieldType) || fieldType.getName().equals("long")) { // NOSONAR check primitive
            return getLong(cell);
        } else if (Duration.class.isAssignableFrom(fieldType)) {
            return getDuration(cell, timeFormat);
        } else if (BigDecimal.class.isAssignableFrom(fieldType)) {
            return new BigDecimal(getString(cell));
        } else if (Map.class.isAssignableFrom(fieldType)) {
            var result = new HashMap<String, Object>();
            if (valueType == null) {
                log.warn("Type of values at map is not defined. String will be used. Please set the annotation ValueType with point to the value class to customize it");
                valueType = String.class;
            }
            result.put(keyOfValue, defineValue(cell, valueType, null, null, nullRender, timeFormat, functionalNullRenderer));
            return ReflectionUtils.castObjectToMap(result, String.class, valueType);
        } else {
            return ReflectionUtils.cast(cell, fieldType);
        }
    }

    private Duration getDuration(C cell, TimeFormat timeFormat) {
        var stringValue = getString(cell);
        if (stringValue.isBlank()) {
            return null;
        }
        if (timeFormat == null) {
            return Duration.parse(stringValue);
        } else {
            var format = timeFormat.value();
            var timeParts = stringValue.split("\\.");
            var value = switch (format) {
                case HOURS -> getHours(timeParts[0]);
                case MINUTES -> getMinutes(timeParts[0]);
                case SECONDS -> getSeconds(timeParts[0]);
                default -> throw new IllegalArgumentException("Format %s is not supported".formatted(format.name()));
            };
            if (timeFormat.withMillis() && timeParts.length > 1) {
                value = value.plusMillis(Long.parseLong(timeParts[1]));
            }
            return value;
        }
    }

    private Duration getMinutes(String stringValue) {
        var rawString = stringValue.split(":");
        return Duration.ZERO.plusMinutes(Long.parseLong(rawString[0])).plusSeconds(Long.parseLong(rawString[1]));
    }

    private Duration getHours(String stringValue) {
        var rawString = stringValue.split(":");
        return Duration.ZERO.plusHours(Long.parseLong(rawString[0])).plusMinutes(Long.parseLong(rawString[1])).plusSeconds(Long.parseLong(rawString[2]));
    }

    private Duration getSeconds(String stringValue) {
        var rawString = stringValue.split(":");
        return Duration.ZERO.plusSeconds(Long.parseLong(rawString[0]));
    }

    private boolean containsValue(R row, int count) {
        for (var cellIndex = 0; cellIndex < count; cellIndex++) {
            var cell = getCell(row, cellIndex);
            if (cell != null
                    && ((!ignoreFormulas && isFormula(cell))
                    || (!isBlank(cell) && !isFormula(cell)))) {
                return true;
            }
        }
        return false;
    }

    private <V> Constructor<V> getConstructor(Class<V> valueClass) {
        var constructor = ReflectionUtils.getConstructor(valueClass);
        if (constructor == null) {
            throw new UnsupportedOperationException("Constructor for %s not found".formatted(valueClass));
        }
        return constructor;
    }

    private <D> D parseChrono(String value, DateTimeFormatter formatter, TemporalQuery<D> query) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(value, query);
    }

    protected <D> D parseChrono(String value, NullRender nullRender, TimeFormat timeFormat,
                                TemporalQuery<D> query, DateTimeFormatter iso) {
        if (value == null) {
            if (nullRender != null) {
                value = nullRender.value();
            } else {
                return null;
            }
        }
        if (timeFormat != null && TimeFormat.Format.CUSTOM.equals(timeFormat.value()) && timeFormat.format() != null) {
            return parseChrono(value, DateTimeFormatter.ofPattern(timeFormat.format()), query);
        } else {
            return parseChrono(value, iso, query);
        }
    }

    /**
     * Преобразование строки в дату время.
     *
     * @param cell       ячейка.
     * @param nullRender обработчик нулевого значения.
     * @param timeFormat формат времени.
     * @param query      функция создания объекта времени.
     * @param iso        ISO формат времени.
     * @return объект времени.
     */
    protected abstract <D> D parseChrono(C cell, NullRender nullRender, TimeFormat timeFormat, TemporalQuery<D> query,
                                         DateTimeFormatter iso);

    /**
     * Получение строки.
     *
     * @param workSheet рабочая страница.
     * @param rowIndex  индекс строки.
     * @return строка.
     */
    protected abstract R getRow(S workSheet, int rowIndex);

    /**
     * Получение количества строк.
     *
     * @param workSheet рабочая страница.
     * @return количество строк.
     */
    protected abstract int getRowsCount(S workSheet);

    /**
     * Получение номера строки.
     *
     * @param row строка.
     * @return номер строки.
     */
    protected abstract int getRowNumber(R row);

    /**
     * Получение номера последней ячейки.
     *
     * @param row строка.
     * @return номер последней ячейки.
     */
    protected abstract int getLastCellNumber(R row);

    /**
     * Получение ячейки.
     *
     * @param row   строка.
     * @param index номер ячейки.
     * @return ячейка.
     */
    protected abstract C getCell(R row, int index);

    /**
     * Проверка пустоты ячейки.
     *
     * @param cell ячейка.
     * @return флаг пустоты.
     */
    protected abstract boolean isBlank(C cell);

    /**
     * Проверка наличия формулы в ячейке.
     *
     * @param cell ячейка.
     * @return флаг наличия формулы.
     */
    protected abstract boolean isFormula(C cell);

    /**
     * Проверка наличия числа в ячейке.
     *
     * @param cell ячейка.
     * @return флаг наличия числа.
     */
    protected abstract boolean isNumber(C cell);

    /**
     * Получение строкового значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract String getString(C cell);

    /**
     * Получение целочисленного значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract Long getLong(C cell);

    /**
     * Получение целочисленного значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract Integer getInteger(C cell);

    /**
     * Получение дробного значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract Double getDouble(C cell);

    /**
     * Получение логического значения.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected abstract Boolean getBoolean(C cell);

    /**
     * Получение значения времени.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected LocalTime getLocalTime(C cell, NullRender nullRender, TimeFormat timeFormat) {
        return parseChrono(cell, nullRender, timeFormat, LocalTime::from, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * Получение значения даты-времени.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected LocalDateTime getLocalDateTime(C cell, NullRender nullRender, TimeFormat timeFormat) {
        return parseChrono(cell, nullRender, timeFormat, LocalDateTime::from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Получение значения даты.
     *
     * @param cell ячейка.
     * @return значение.
     */
    protected LocalDate getLocalDate(C cell, NullRender nullRender, TimeFormat timeFormat) {
        return parseChrono(cell, nullRender, timeFormat, LocalDate::from, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Подготовка страницы к чтению.
     *
     * @param workSheet страница.
     */
    protected void prepareWorksheet(S workSheet) {
    }

}
