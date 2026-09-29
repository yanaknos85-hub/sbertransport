package ru.sberbank.ditsib.geo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;
import ru.sberbank.ditsib.geo.service.DataExtractor;
import ru.sberbank.ditsib.geo.service.RegionService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Реализация извлечения данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class DataExtractorImpl implements DataExtractor {
    
    private final RegionService regionService;
    private final MapUtils mapUtils;

    @Override
    public <V> V getValue(
            Map<String, Object> map, @NonNull String fieldPath,
            Class<V> valueClass
                                 ) {
        var function = getFunction(fieldPath);
        if (function != null) {
            if ("region".equals(function.getKey())) {
                var value = mapUtils.extractNode(map, function.getValue(), String.class);
                var item = region(value);
                var subPath = fieldPath.replace(String.format("%s(%s)", function.getKey(), function.getValue()),
                        "").replace(".", "");
                return mapUtils.extractNode(item, subPath, valueClass);
            } else if ("exact".equals(function.getKey())) {
                return new ObjectMapper().convertValue(function.getValue(), valueClass);
            }
        }
        return mapUtils.extractNode(map, fieldPath, valueClass);
    }
    
    @Override
    public <V> V getValueOrDefault(
            Map<String, Object> map, @NonNull String fieldPath,
            V defaultValue
                                          ) {
        var result = getValue(map, fieldPath, defaultValue.getClass());
        if (result == null) {
            return defaultValue;
        }
        return ReflectionUtils.cast(result);
    }
    
    /**
     * Получение региона по идентификатору.
     *
     * @param id идентификатор региона.
     *
     * @return map с данными региона.
     */
    private Map<String, Object> region(String id) {
        try {
            return regionService.get(id);
        } catch (GeoApiException e) {
            log.warn(e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Получение функции из пути к полю.
     *
     * @param fieldPath путь к полю.
     *
     * @return функция с аргументом.
     */
    private Map.Entry<String, String> getFunction(String fieldPath) {
        if (fieldPath == null) {
            return null;
        }
        var matcher = Pattern.compile("(\\w[a-zA-Z0-9]*)(\\(.*\\))").matcher(fieldPath);
        if (matcher.find()) {
            return new AbstractMap.SimpleEntry<>(matcher.group(1),
                                                 matcher.group(2).replace("(", "").replace(")", ""));
        }
        return null;
    }
    
}
