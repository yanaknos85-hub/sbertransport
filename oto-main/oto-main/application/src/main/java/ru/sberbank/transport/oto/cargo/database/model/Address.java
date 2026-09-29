package ru.sberbank.transport.oto.cargo.database.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity describing address in request
 */
@Entity
@Table(schema = "oto_cargo", name = "address")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    
    @Id
    private UUID id;
    
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
    @Column(name = "exist_in_vsp_tb_registry")
    private Boolean existInVspGosbTbRegistry;
    
    /**
     * Полная строка адреса
     */
    @Column(name = "address_string")
    private String addressString;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address other = (Address) o;
        return Objects.equals(country, other.country) &&
               Objects.equals(region, other.region) &&
               Objects.equals(city, other.city) &&
               Objects.equals(street, other.street) &&
               Objects.equals(house, other.house) &&
               Objects.equals(building, other.building) &&
               Objects.equals(structure, other.structure);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(country, region, city, street, house, building, structure);
    }
    
    @Override
    public String toString() {
        return region + ", " + street + ", " + house;
    }
    
}
