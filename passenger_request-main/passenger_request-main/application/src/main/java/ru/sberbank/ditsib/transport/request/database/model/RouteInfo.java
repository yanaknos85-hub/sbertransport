package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Фактическая информация по точкам маршрута
 */
@Entity
@Table(schema = "request", name = "waypoint_info")
@Getter
@Setter
@ToString(exclude = "taxiTrip")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RouteInfo {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    
    /**
     * Ссылка на поездку
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id")
    private TaxiTrip taxiTrip;
    
    /**
     * Порядковый номер точки маршрута
     */
    @Column(name = "waypoint_id")
    private Integer waypointId;
    
    /**
     * Фактическое время прибытия машины на точку
     */
    @Column(name = "fact_time_arrive")
    private LocalDateTime factTimeArrive;
}
