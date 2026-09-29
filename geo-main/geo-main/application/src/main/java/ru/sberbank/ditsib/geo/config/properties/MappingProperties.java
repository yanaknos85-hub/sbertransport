package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Properties for mapping JSON.
 */
@Getter
@Setter
public class MappingProperties {
    
    /**
     * Root node of mapping.
     */
    private String root;
    
    /**
     * Fields mapping.
     */
    private MappingFields fields = new MappingFields();
    
}
