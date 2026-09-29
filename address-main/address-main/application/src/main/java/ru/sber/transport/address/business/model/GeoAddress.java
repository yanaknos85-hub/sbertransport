package ru.sber.transport.address.business.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Бизнес-модель адреса.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeoAddress implements Address {
    
    /**
     * Идентификатор.
     */
    private UUID id;
    
    private String country;
    
    private String region;
    
    private String city;
    
    private String street;
    
    private String house;
    
    private String building;
    
    private String structure;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;

    @Override
    public int hashCode() {
        return Objects.hash(getCountry(), getRegion(), getCity(), getStreet(), getHouse(), getLongitude(), getLatitude(), getBuilding(), getStructure());
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return equals(obj, getClass());
    }

    public boolean equals(Object source, Class<? extends Address> targetClass) {
        if (!targetClass.isAssignableFrom(source.getClass())) {
            return false;
        }

        var sourceAddress = (Address) source;
        if ((getLongitude() != null && sourceAddress.getLongitude() != null && getLongitude().equals(sourceAddress.getLongitude()))
            && (getLatitude() != null && sourceAddress.getLatitude() != null && getLatitude().equals(sourceAddress.getLatitude()))) {
            return true;
        }

        return (getRegion() == null && sourceAddress.getRegion() == null) ||
               (getRegion() != null && getRegion().equalsIgnoreCase(sourceAddress.getRegion()))
               && ((getCity() == null && sourceAddress.getCity() == null) ||
                   (getCity() != null && getCity().equalsIgnoreCase(sourceAddress.getCity())))
               && ((getStreet() == null && sourceAddress.getStreet() == null) ||
                   (getStreet() != null && getStreet().equalsIgnoreCase(sourceAddress.getStreet())))
               && ((getBuilding() == null && sourceAddress.getBuilding() == null) ||
                   (getBuilding() != null && getBuilding().equalsIgnoreCase(sourceAddress.getBuilding())))
               && ((getStructure() == null && sourceAddress.getStructure() == null) ||
                   (getStructure() != null && getStructure().equalsIgnoreCase(sourceAddress.getStructure())))
               && ((getHouse() == null && sourceAddress.getHouse() == null) ||
                   (getHouse() != null && getHouse().equalsIgnoreCase(sourceAddress.getHouse())));
    }

    @Override
    public String toString() {
        return "%s, %s, %s, %s".formatted(getRegion(), getCity(), getStreet(), getHouse());
    }
}
