package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDTO {
    
    private UUID id;
    
    private String brand;
    
    private String model;
    
    private String stateNumber;
    
    private List<TaskDTO> trips;
}
