package ru.sberbank.ditsib.transport.tariff.dto;

import lombok.Builder;

@Builder
public class WorkGroup {
    
    private final String organizationName;
    
    private final String regionName;
    
    private final String contractNumber;
    
    public String getFullName() {
        return String.format("%s/%s/%s", organizationName, regionName, contractNumber);
    }
    
}
