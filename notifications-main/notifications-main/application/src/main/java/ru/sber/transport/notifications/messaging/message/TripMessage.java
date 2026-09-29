package ru.sber.transport.notifications.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sberbank.ditsib.transport.messaging.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @deprecated удалить в рамках перевода сервиса на новую версию kafka-functional
 */
@Deprecated(forRemoval = true)
@Jacksonized
@SuperBuilder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripMessage implements Message<UUID> {

    private UUID id;

    private TripType type;

    private LocalDateTime startTime;

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

    private UUID plannedShiftId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalData(
            Integer passengerCount,
            String taxiClass,
            Duration driverWaitingTime,
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
