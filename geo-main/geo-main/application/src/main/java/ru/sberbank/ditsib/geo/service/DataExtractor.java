package ru.sberbank.ditsib.geo.service;

import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * Вспомогательная утилита для извлечения данных.
 */
public interface DataExtractor {
    
    /**
     * Получение значение из источника.
     *
     * @param map источник.
     * @param fieldPath путь к свойству.
     * @param valueClass класс значения.
     * @param <V> тип значения.
     *
     * @return значение.
     */
    <V> V getValue(
            Map<String, Object> map, @NonNull String fieldPath,
            Class<V> valueClass
                          );
    
    /**
     * Получение значение из источника.
     *
     * @param map источник.
     * @param fieldPath путь к свойству.
     * @param defaultValue значение по-умолчанию.
     * @param <V> тип значения.
     *
     * @return значение.
     */
    <V> V getValueOrDefault(
            Map<String, Object> map, @NonNull String fieldPath,
            V defaultValue
                                   );
}
