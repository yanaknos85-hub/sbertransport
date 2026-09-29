package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.JSON;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverBusynessRawResponse {

    private DriverData driverData;

    private TripData tripData;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DriverData {

        private UUID id;

        private String humanReadableId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TripData {

        private UUID id;

        private TripStatus status;

        private Long digitId;

        private JSON requests;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime startTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime dispatcherStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime endTime;

        private VehicleData vehicleData;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VehicleData {

        private UUID id;

        private String stateNumber;

        private String model;

        private String brand;
    }
}
