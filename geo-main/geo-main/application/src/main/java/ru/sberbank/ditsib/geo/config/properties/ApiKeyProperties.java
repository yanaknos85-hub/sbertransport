package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Properties of API key.
 */
@Getter
@Setter
public class ApiKeyProperties {
    
    /**
     * Name of field with API key.
     */
    private String field = "key";
    
    /**
     * Value of API key.
     */
    private String value;
    
}
