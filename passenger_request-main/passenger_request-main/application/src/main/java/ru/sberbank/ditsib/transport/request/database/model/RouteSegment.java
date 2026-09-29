package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Расчётные параметры маршрута между двумя точками
 */
@Entity
@Table(schema = "request", name = "route_segment")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteSegment {
    
    /**
     * Identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * cost
     */
    @Column(name = "cost")
    private double cost;
    
    /**
     * distance
     */
    @Column(name = "distance")
    private double distance;
    
    /**
     * time
     */
    @Column(name = "time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration time;
    
    /**
     * Sequence of waypoints
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn(name = "ordering_index")
    @JoinTable(schema = "request", name = "route_segment_coordinates")
    @Builder.Default
    private List<Coordinates> coordinates = new ArrayList<>();
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    @PreRemove
    private void tearDown() {
        coordinates.clear();
        this.request = null;
    }
}
