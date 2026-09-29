package ru.sberbank.ditsib.transport.vehicle.database.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface VehicleShortProjection {
    UUID getId();
    
    String getBrand();
    
    String getModel();
    
    String getEngineType();
    
    String getFuelType();
    
    int getEngineCapacity();
    
    BigDecimal getEnginePower();
    
    int getFuelTankVolume();
    
    String getDrive();
    
    boolean getSpareWheelHolderInstalled();
    
    boolean getMudguardInstalled();

    String getBodyType();

    String getTransmissionType();

    int getWeight();

    int getHeight();

    int getWidth();

    int getLength();

    int getYearManufactureBegin();

    Integer getYearManufactureEnd();
}