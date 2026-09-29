package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Response format properties.
 */
@Getter
@Setter
public class ResponseFormatProperties {
    
    /**
     * Name of field to declare format.
     */
    private String field = "format";
    
    /**
     * Value of format.
     */
    private String value = "json";
    
    private MappingProperties mapping = new MappingProperties();
    
}
