package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Entity describing address in request
 */
@Entity
@Table(schema = "request", name = "address")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Latitude
     */
    @Column(nullable = false)
    private double latitude;
    
    /**
     * Longitude
     */
    @Column(nullable = false)
    private double longitude;
    
    /**
     * Country.
     */
    @Column
    private String country;
    
    /**
     * Region.
     */
    @Column
    private String region;
    
    /**
     * City.
     */
    @Column
    private String city;
    
    /**
     * Street.
     */
    @Column
    private String street;
    
    /**
     * House.
     */
    @Column
    private String house;
    
    /**
     * Building.
     */
    @Column
    private String building;
    
    /**
     * Structure.
     */
    @Column
    private String structure;
    
    /**
     * Value exist in VSP/GOSB/TB registry
     */
    @Column(name = "exist_in_vsp_tb_registry", nullable = false)
    private boolean existInVspGosbTbRegistry;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address other = (Address) o;
        return (Double.compare(other.latitude, latitude) == 0) &&
               Double.compare(other.longitude, longitude) == 0 &&
               (country == null ? other.country == null : country.equals(other.country)) &&
               (region == null ? other.region == null : region.equals(other.region)) &&
               (city == null ? other.city == null : city.equals(other.city)) &&
               (street == null ? other.street == null : street.equals(other.street)) &&
               (house == null ? other.house == null : house.equals(other.house)) &&
               (building == null ? other.building == null : building.equals(other.building)) &&
               (structure == null ? other.structure == null : structure.equals(other.structure));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, country, region, city, street, house, building, structure);
    }
    
    @Override
    public String toString() {
        return country + ", " +
               region + ", " +
               city + ", " +
               street + ", " +
               house + ", " +
               building + ", " +
               structure;
    }
    
    public String toStringTrimmed() {
        String str = toString();
        List<String> list = new ArrayList<>();
        String[] strSplit = str.split(",");
        for (String s : strSplit) {
            if (s == null || "null".equals(s.trim())) {
                continue;
            }
            list.add(s);
        }
        return list.stream().collect(Collectors.joining(","));
    }
}
