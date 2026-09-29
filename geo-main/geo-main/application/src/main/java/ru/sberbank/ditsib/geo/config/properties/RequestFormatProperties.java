package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * Format of requests.
 */
@Getter
@Setter
public class RequestFormatProperties {
    
    private MappingProperties mapping = new MappingProperties();
    
}
