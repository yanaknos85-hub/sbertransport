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
public class UnitProperties implements HasDefaultValue<DistanceUnit> {
    
    /**
     * Default unit.
     */
    @Accessors(prefix = "unit")
    private DistanceUnit unitDefault = DistanceUnit.KILOMETERS;
    
    /**
     * Kilometers string mapping.
     */
    private String kilometers = "kilometers";
    
    /**
     * Miles string mapping.
     */
    private String miles = "miles";
    
    /**
     * Path to unit options.
     */
    private String path = "";
    
}
