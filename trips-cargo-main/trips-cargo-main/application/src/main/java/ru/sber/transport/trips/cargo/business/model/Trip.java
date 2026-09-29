package ru.sber.transport.trips.cargo.business.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Поездка.
 */
@Getter
@Setter
public final class Trip {
    private UUID id;
    private OffsetDateTime startTime;
    private OffsetDateTime dispatcherStartTime;
    private OffsetDateTime endTime;
    private TripStatus status = TripStatus.WAITING_FOR_ASSIGNMENT;
    private List<Waypoint> waypoints = new ArrayList<>();
    private UUID contractorId;
    private Set<CargoRequest> requests = new HashSet<>();
    private Long digitId;
    private Double factDistance;
    private UUID driverId;
    private UUID vehicleId;
    private Double capacity;
    private Long loadersWorkTime;
    private UUID dispatcherId;
    private OffsetDateTime arrivedDate;
    private String humanReadableId;
    private String routeHumanReadableId;
    private Integer autoassignCounter = 0;
    private OffsetDateTime creationTime;
    private Long expectedCost;
    private Integer factCost;
    private OffsetDateTime finishTime;
    private Double expectedDistance;
    private UUID plannedShiftId;
    private Duration driverWaitingTime;
    private Integer loaders = 0;
    private String timeZone;
    private String comment;
    private String authorName;
    private String authorPhone;
    private UUID autoparkId;
}
