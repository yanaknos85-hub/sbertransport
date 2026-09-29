package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleBusynessDTO {

    private UUID vehicleId;

    private List<TripData> trips;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TripData {

        private UUID id;

        private String humanReadableId;

        private TripStatus status;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factEndTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedEndTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime driverProcessingTime;

        private boolean planning;
    }
}
