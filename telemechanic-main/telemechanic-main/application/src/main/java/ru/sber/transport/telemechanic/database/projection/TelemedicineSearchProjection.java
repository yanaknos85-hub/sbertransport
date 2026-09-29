package ru.sber.transport.telemechanic.database.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TelemedicineSearchProjection {
    
    UUID getId();
    
    String getHumanReadableId();
    
    UUID getEwbId();
    
    String getEwbHumanReadableId();
    
    String getOrganizationName();
    
    String getStatus();
    
    LocalDateTime getCreationTime();
    
    String getDriverFullName();
    
    void setId(UUID id);
    
    void setHumanReadableId(String value);
    
    void setEwbId(UUID id);
    
    void setEwbHumanReadableId(String value);
    
    void setOrganizationName(String value);
    
    void setStatus(String value);
    
    void setCreationTime(LocalDateTime value);
    
    void setDriverFullName(String value);
}
