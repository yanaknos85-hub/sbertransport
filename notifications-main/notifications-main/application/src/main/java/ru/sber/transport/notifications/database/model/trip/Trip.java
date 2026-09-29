package ru.sber.transport.notifications.database.model.trip;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.request.Vehicle;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity of trip.
 */
@Getter
@Setter
public class Trip {

    /**
     * Identifier.
     */
    private UUID id;

    /**
     * Time of trip started.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * Time of trip finished.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Status of trip.
     */
    @Column
    private String status;

    /**
     * Class of trip.
     */
    @Column(name = "trip_class")
    private String tripClass;

    /**
     * Data of requests in trip.
     */
    private List<Map<String, Object>> requests = new ArrayList<>();

    /**
     * Data of waypoints in trip.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "waypoints")
    private List<Map<String, Object>> waypoints = new ArrayList<>();

    /**
     * Digit ID of contractor for human readable ID.
     */
    private String humanReadableId;

    private Driver driver;

    private Vehicle vehicle;

    private UUID plannedShiftId;

}
