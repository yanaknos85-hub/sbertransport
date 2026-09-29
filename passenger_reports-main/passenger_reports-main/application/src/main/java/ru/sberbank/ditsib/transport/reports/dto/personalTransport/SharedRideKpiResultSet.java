package ru.sberbank.ditsib.transport.reports.dto.personalTransport;

import lombok.Data;

import java.util.UUID;

@Data
public class SharedRideKpiResultSet {
    private UUID id;
    private SharedRideKpiDTO sharedRideKpi;
    
    public SharedRideKpiResultSet(UUID id, Double totalCost, Double totalDistanceKm, Integer totalTimeMin) {
        this.id = id;
        sharedRideKpi = SharedRideKpiDTO.builder()
                        .totalCost(totalCost)
                        .totalDistanceKm(totalDistanceKm)
                        .totalTimeMin(totalTimeMin)
                        .build();
    }
}
