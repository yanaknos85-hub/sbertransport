package ru.sberbank.ditsib.geo.config.properties.geocoding;

import java.util.HashMap;

/**
 * Маппинг типов запроса.
 */
public class RequestTypeMapping extends HashMap<String, String> {
    
    /**
     * Тип запроса.
     */
    public String getType() {return  get("type");}

    /**
     * Get value of field with name.
     *
     * @param fieldName name of field to get value.
     * @return value of field with name.
     */
    public String get(String fieldName) {
        return getOrDefault(fieldName, null);
    }
}
