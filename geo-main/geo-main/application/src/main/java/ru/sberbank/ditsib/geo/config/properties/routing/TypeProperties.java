package ru.sberbank.ditsib.geo.config.properties.routing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.sberbank.ditsib.geo.config.properties.HasDefaultValue;

/**
 * Properties of unit.
 */
@Getter
@Setter
@NoArgsConstructor
public class TypeProperties implements HasDefaultValue<RouteType> {
    
    /**
     * Default unit.
     */
    @Accessors(prefix = "type")
    private RouteType typeDefault = RouteType.CAR;
    
    /**
     * Kilometers string mapping.
     */
    private String car = "car";
    
    /**
     * Miles string mapping.
     */
    private String pedestrian = "pedestrian";
    
    /**
     * Path to unit options.
     */
    private String path = "";
    
}
