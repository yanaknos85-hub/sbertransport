package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Embeddable
public class GeoWaypoint {
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
    
    @NotNull
    @Column
    private LocalDateTime geoTime;
}
