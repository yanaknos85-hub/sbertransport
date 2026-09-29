package ru.sber.transport.trips.cargo.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.*;

@Jacksonized
@SuperBuilder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripMessage implements Message<UUID> {

    private UUID id;

    private TripType type;

    private LocalDateTime startTime;

    private LocalDateTime dispatcherStartTime;

    private LocalDateTime endTime;

    private String status;

    private AdditionalData additional;

    private UUID contractorId;

    @Builder.Default
    private List<Map<String, Object>> requests = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> waypoints = new ArrayList<>();

    private long digitId;

    private long contractorDigitId;

    private UUID driverId;

    private UUID vehicleId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalData(
            Double capacity
    ){}

    public enum TripType {

        /**
         * Пассажиры.
         */
        PASSENGER,

        /**
         * Грузы.
         */
        CARGO
    }
}
