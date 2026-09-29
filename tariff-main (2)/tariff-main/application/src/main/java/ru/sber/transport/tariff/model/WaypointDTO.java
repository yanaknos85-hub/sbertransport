package ru.sber.transport.tariff.model;

import lombok.Builder;
import lombok.Getter;
import ru.sberbank.utils.reflection.DoubleUtil;

import java.time.Duration;

/**
 * Object with data about waypoint.
 */
@Builder
@Getter
public class WaypointDTO {
    
    /**
     * Country.
     */
    private final String country;
    
    /**
     * Region.
     */
    private final String region;
    
    /**
     * Район.
     */
    private final String district;
    /**
     * City.
     */
    private final String city;
    
    /**
     * Street.
     */
    private final String street;
    
    /**
     * House.
     */
    private final String house;
    
    /**
     * Building.
     */
    private final String building;
    
    /**
     * Structure.
     */
    private final String structure;
    
    private final Duration waitTime;
    
    private final Double latitude;
    
    private final Double longitude;
    
    private final boolean checkinAutomatic;
    
    private final boolean checkinManual;
    
    private final String absenceReason;
    
    private final boolean checkinOnlyManual;
    
    private final boolean active;
    
    private final boolean existInVspGosbTbRegistry;
    
    
    /**
     * Определение эквивалентности по координатам.
     *
     * @param toCompare объект для сравнения.
     * @return <code>true</code> если объекты эквивалентны.
     */
    public boolean equalsByCoords(WaypointDTO toCompare) {
        if (toCompare == null) {
            return false;
        }
        if (toCompare.getLatitude() == 0 || toCompare.getLongitude() == 0) {
            return false;
        }
        return DoubleUtil.doubleEqualsWithPrecision(getLatitude(),
                                                    toCompare.getLatitude(),
                                                    DoubleUtil.EPSILON)
               && DoubleUtil.doubleEqualsWithPrecision(getLongitude(), toCompare.getLongitude(),
                                                       DoubleUtil.EPSILON);
    }
}