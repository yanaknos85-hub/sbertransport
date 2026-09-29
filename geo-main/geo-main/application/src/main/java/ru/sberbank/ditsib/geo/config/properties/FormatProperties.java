package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Format of request/response.
 */
@Getter
@Setter
public class FormatProperties {
    
    private MappingProperties request = new MappingProperties();
    
    private MappingProperties response = new MappingProperties();
    
}
