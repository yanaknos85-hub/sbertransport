package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Местоположение автомобиля
 */
@Entity
@Table(schema = "request", name = "route_history_element")
@Getter
@Setter
@Builder
@ToString(exclude = "taxiTrip")
@AllArgsConstructor
@NoArgsConstructor
public class RouteHistoryElement {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    /**
     * Родительский объект поездки
     */
    @JoinColumn(name = "trip_id", nullable = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private TaxiTrip taxiTrip;
    
    /**
     * широта
     */
    @NotNull(message = "Latitude of last car position cannot be null")
    @Column(name = "latitude")
    private Double latitude;
    
    /**
     * Долгота
     */
    @NotNull(message = "Longitude of last car position cannot be null")
    @Column(name = "longitude")
    private Double longitude;
    
    /**
     * время
     */
    @NotNull(message = "geoTime of last car position cannot be null")
    @Column(name = "geo_time")
    private LocalDateTime geoTime;
}
