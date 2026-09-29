package ru.sber.transport.trip.business.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Объект заявки.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Request {
    private UUID id;
    private UUID authorId;
    private String humanReadableId;
    private Employee author;
    private UUID passengerId;
    private Employee passenger;
    private UUID organizationId;
    private String taxiClass;
    private int passengerCount;
    private ExpectedData expected;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime creationTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime desiredDate;
    private UUID rideId;

    @JsonProperty("coop")
    private boolean coopTrip;
    private boolean suburb;
    private String timeZone;
    private List<String> requestOptions;
    private UUID tariffId;
    private Map<String, Object> tariff;
    private UUID contractorId;
    private String status;
    @JsonAlias("commentForDriver")
    private String comment;
    private List<Waypoint> waypoints;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration driverWaitingTime;
    private Double factDistance;
    private String transportType;
    private boolean sharedRideOwner;
    private boolean vip;
    private String groupTransferClass;
    private Map<String, Object> information;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Request req && req.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Request.class.getCanonicalName(), id.hashCode());
    }

}