package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    
    private UUID id;
    
    private String brand;
    
    private String model;
    
    private String stateNumber;
    
    private String color;
    
    private UUID autoparkId;


}
