package ru.sber.transport.notifications.database.model.request;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
public class Address {
    
    /**
     * Country.
     */
    private final String country;
    
    /**
     * Region.
     */
    private final String region;
    
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
    
    /**
     * Latitude.
     */
    private final double latitude;
    
    /**
     * Longitude.
     */
    private final double longitude;

    public String toFullAddressString(CharSequence delimiter) {
        return Stream.of(
                        this.getCity(),
                        this.getStreet(),
                        this.getHouse(),
                        this.getBuilding(),
                        this.getStructure())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(delimiter));
    }

    public String toFullAddressString() {
        return toFullAddressString(", ");
    }
}
