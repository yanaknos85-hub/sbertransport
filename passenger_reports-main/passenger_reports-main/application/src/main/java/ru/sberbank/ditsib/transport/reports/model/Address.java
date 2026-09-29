package ru.sberbank.ditsib.transport.reports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity describing address in request
 */
@Entity
@Table(schema = "reports", name = "address")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address other = (Address) o;
        return ((country == null ? other.country == null : country.equals(other.country))) &&
                ((region == null ? other.region == null : region.equals(other.region))) &&
                ((city == null ? other.city == null : city.equals(other.city))) &&
                ((street == null ? other.street == null : street.equals(other.street))) &&
                ((house == null ? other.house == null : house.equals(other.house))) &&
                ((building == null ? other.building == null : building.equals(other.building))) &&
                ((structure == null ? other.structure == null : structure.equals(other.structure)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, region, city, street, house, building, structure);
    }
    
    @Override
    public String toString() {
        return (region != null ? region + ", " : "") +
               ((region != null && city != null && !region.equals(city) || region == null && city != null) ? city + ", " : "") +
               (street != null ? street + ", " : "") +
               (house != null ? house : "");
    }

}
