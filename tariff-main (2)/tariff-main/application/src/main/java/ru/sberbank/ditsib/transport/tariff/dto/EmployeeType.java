package ru.sberbank.ditsib.transport.tariff.dto;

import java.util.UUID;

public interface EmployeeType {
    UUID getId();
    
    UUID getUserId();
    
    String getFirstName();
    
    String getLastName();
    
    String getPatronymic();
    
    UUID getDepartmentId();
    
    UUID getOrganizationId();
}
