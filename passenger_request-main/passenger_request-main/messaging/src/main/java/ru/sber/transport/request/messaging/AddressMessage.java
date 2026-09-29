package ru.sber.transport.request.messaging;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Jacksonized
@SuperBuilder
@Getter
@ToString
public class AddressMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private final UUID id;
    
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
    private final Double latitude;
    
    /**
     * Longitude.
     */
    private final Double longitude;
    
    /**
     * Value exist in VSP/GOSB/TB Registry
      */
    private final Boolean existInVspGosbTbRegistry;
}
