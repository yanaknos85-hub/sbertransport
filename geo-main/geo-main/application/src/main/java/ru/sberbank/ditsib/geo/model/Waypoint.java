package ru.sberbank.ditsib.geo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

/**
 * Объект путевой точки.
 */
@Setter
@Getter
@SuperBuilder
public class Waypoint extends Address {
    
    /**
     * Время ожидания.
     */
    private Duration waitTime;
    
    /**
     * Создание путевой точки для адреса.
     *
     * @param address исходный адрес.
     */
    public Waypoint(
            Address address
                   ) {
        super(address.getId(), address.getCountry(), address.getRegion(), address.getDistrict(),
              address.getCity(),
              address.getSettlement(),
              address.getLivingArea(),
              address.getPlace(),
              address.getStreet(),
              address.getHouse(),
                address.getLatitude(),
                address.getLongitude(),
                address.getAttributeGroups(),
                address.getNameEx(),
                address.getIsPaid(),
                address.getPoint(),
                address.getFullName(),
                address.getName(),
                address.getType(),
                address.getGeometry(),
                address.getObjectId(),
                address.getPurposeName());
        if (address instanceof Waypoint) {
            this.waitTime = ((Waypoint) address).getWaitTime();
        }
    }
    
    /**
     * Создание путевой точки для адреса.
     *
     * @param address исходный адрес.
     * @param waitTime время ожидания.
     */
    public Waypoint(
            Address address,
            Duration waitTime
                   ) {
        super(address.getId(), address.getCountry(), address.getRegion(), address.getDistrict(),
              address.getCity(),
                address.getSettlement(),
                address.getLivingArea(),
                address.getPlace(),
              address.getStreet(),
              address.getHouse(),
                address.getLatitude(),
                address.getLongitude(),
                address.getAttributeGroups(),
                address.getNameEx(),
                address.getIsPaid(),
                address.getPoint(),
                address.getFullName(),
                address.getName(),
                address.getType(),
                address.getGeometry(),
                address.getObjectId(),
                address.getPurposeName());
        this.waitTime = waitTime;
    }
}
