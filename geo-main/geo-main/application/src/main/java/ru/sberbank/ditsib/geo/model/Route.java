package ru.sberbank.ditsib.geo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Object with data with requested route.
 */
@Getter
@Setter
@Builder
public class Route implements HasTime, HasDistance {
    
    /**
     * Distance.
     */
    private Double distance;
    
    /**
     * Time.
     */
    private Duration time;
    
    /**
     * Coordinates of route.
     */
    @Builder.Default
    private final List<Segment> segments = new ArrayList<>();
    
    /**
     * List of maneuvers.
     */
    @Builder.Default
    private final List<Waypoint> waypoints = new ArrayList<>();
}
