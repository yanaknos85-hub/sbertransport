package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.JSON;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleBusynessRawResponse {

    private UUID vehicleId;

    private TripData tripData;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TripData {

        private UUID id;

        private TripStatus status;

        private Long digitId;

        private JSON requests;

        private JSON waypoints;

        private String timeZone;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factEndTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedEndTime;
    }
}
