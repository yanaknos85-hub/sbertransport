package ru.sber.transport.notifications.database.model.request;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.sber.transport.notifications.database.model.TripPurpose;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Заявка на поездку.
 */
@Entity
@Table(schema = "notifications_request", name = "trip")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {
    
    @Id
    @NotNull
    private UUID id;
    
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    @Column(name = "author_id")
    private UUID authorId;
    
    @Transient
    private Employee author;
    
    @Column(name = "passenger_id")
    private UUID passengerId;
    
    @Transient
    private Employee passenger;
    
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    @Column(name = "finished_time")
    private LocalDateTime finishedTime;
    
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    @Column(name = "trip_class")
    private String tripClass;
    
    @Type(JsonBinaryType.class)
    @Column(name = "waypoints")
    private List<Waypoint> waypoints;
    
    @Column(name = "approval_id")
    private UUID approvalId;
    
    @Column(name = "limit_id")
    private UUID limitId;
    
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "coop")
    private boolean coopTrip;
    
    @Column(name = "desired_date")
    private LocalDateTime desiredDate;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "passenger_count")
    private int passengerCount;
    
    @Column(name = "approval_state")
    private String approvalState;
    
    @Type(JsonBinaryType.class)
    @Column(name = "expected")
    private ExpectedData expected;
    
    @Column(name = "purpose")
    private UUID purposeId;
    
    @Transient
    private TripPurpose purpose;
    
    @Column(name = "comment_for_driver")
    private String commentForDriver;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_ride_id")
    private SharedRide sharedRide;

    @Column(name = "status_code")
    private Integer statusCode;
    
    @Transient
    private TaxiTrip taxiTrip;
    
    @Transient
    private TaxiTariff tariff;
    
    @Transient
    private String approveStatusDescription;

    @Transient
    private String requestStatusDescription;

    @Transient
    private VehicleDTO vehicle;

    @Transient
    private DriverDTO driver;

    @Column(name = "time_zone")
    private String timeZone;

    @Transient
    private String localDesiredDate;

    @Transient
    private String localDesiredTime;

    @Transient
    private String departureAddress;

    @Transient
    private String destinationAddress;

    @Transient
    private String carInfo;

    @Transient
    private String driverFio;

    @Transient
    private Map<String, Object> information = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripRequest )) return false;
        return id.equals(((TripRequest) o).getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
}
