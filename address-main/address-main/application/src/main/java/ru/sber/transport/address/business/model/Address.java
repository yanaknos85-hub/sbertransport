package ru.sber.transport.address.business.model;

import java.math.BigDecimal;
import java.util.UUID;

public interface Address {

    UUID getId();
    
    /**
     * Страна.
     */
    String getCountry();

    /**
     * Регион.
     */
    String getRegion();

    /**
     * Город.
     */
    String getCity();

    /**
     * Улица.
     */
    String getStreet();

    /**
     * Дом.
     */
    String getHouse();

    /**
     * Широта.
     */
    BigDecimal getLongitude();

    /**
     * Долгота.
     */
    BigDecimal getLatitude();

    /**
     * Корпус.
     */
    String getBuilding();

    /**
     * Строение.
     */
    String getStructure();
    
}
