package ru.sberbank.ditsib.transport.vehicle.database.projection;

import java.util.UUID;

public interface TransportShortProjection {
    UUID getId();
    
    String getModel();
    
    String getBrand();
    
    String getStateNumber();
    
    void setId(UUID value);
    
    void setModel(String value);
    
    void setBrand(String value);
    
    void setStateNumber(String value);
}
