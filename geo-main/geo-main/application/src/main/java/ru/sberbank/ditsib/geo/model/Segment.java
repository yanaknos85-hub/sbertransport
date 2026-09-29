package ru.sberbank.ditsib.geo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data of points.
 */
@Getter
@Setter
@Builder
public class Segment implements HasDistance, HasTime {
    
    /**
     * Distance.
     */
    private Double distance;
    
    /**
     * Time.
     */
    private Duration time;
    
    /**
     * List of maneuvers.
     */
    @Builder.Default
    private final List<Coordinates> coordinates = new ArrayList<>();
    
}
