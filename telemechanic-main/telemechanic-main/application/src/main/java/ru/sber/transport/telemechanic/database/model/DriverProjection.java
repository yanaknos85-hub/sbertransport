package ru.sber.transport.telemechanic.database.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DriverProjection {
    
    UUID getId();
    
    String getPersonnelNumber();
    
    String getFullName();
    
    String getOrganizationName();
    
    String getDepartmentName();
    
    String getTin();
    
    String getSeries();
    
    String getNumber();
    
    LocalDate getIssueDate();
    
    LocalDate getExpiryDate();
    
    boolean isActive();
    
    List<String> getCategoryNames();
    
    String getSnils();
    
}
