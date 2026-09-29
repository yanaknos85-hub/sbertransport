package ru.sber.transport.telemechanic.database.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface DriverByFioProjection {
    
    UUID getId();
    
    String getPersonnelNumber();
    
    String getFullName();
    
    String getOrganizationName();
    
    UUID getDepartmentId();
    
    String getDepartmentName();
    
    String getTin();
    
    UUID getDrivingLicenceId();
    
    String getSeries();
    
    String getNumber();
    
    LocalDate getIssueDate();
    
    void setId(UUID value);
    
    void setPersonnelNumber(String value);
    
    void setFullName(String value);
    
    void setOrganizationName(String value);
    
    void setDepartmentId(UUID value);
    
    void setDepartmentName(String value);
    
    void setTin(String value);
    
    void setDrivingLicenceId(UUID value);
    
    void setSeries(String value);
    
    void setNumber(String value);
    
    void setIssueDate(LocalDate value);
    
    
}
