package ru.sberbank.ditsib.transport.reports.dto.taxi;

import lombok.Data;

import java.util.UUID;

@Data
public class ContractorResultSet {
    
    private UUID id;
    
    private String name;
    
    public ContractorResultSet(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
