package ru.sberbank.ditsib.geo.config.properties.geocoding;

import java.util.HashMap;

/**
 * Маппинг типов сортировок.
 */
public class SortMapping extends HashMap<String, String> {
    
    /**
     * Маппинг расстояния.
     */
    public String getDistance() {
        return get("distance");
    }
    
    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get value of field.
     *
     * @param fieldName name of field to get value.
     * @return value from field with name.
     */
    public String get(String fieldName) {
        return getOrDefault(fieldName, null);
    }
}
