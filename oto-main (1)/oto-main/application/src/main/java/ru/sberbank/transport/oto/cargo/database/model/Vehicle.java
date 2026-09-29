package ru.sberbank.transport.oto.cargo.database.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {
    
    private UUID id;
    
    private String brand;
    
    private String model;
    
    private String stateNumber;
    
    private String color;
    
    private UUID autoparkId;
    
    
}

