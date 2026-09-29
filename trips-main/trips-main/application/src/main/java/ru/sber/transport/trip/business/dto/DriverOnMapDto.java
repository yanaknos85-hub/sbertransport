package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DriverOnMapDto {
    private UUID id;
    private String humanReadableId;
    private CurrentShift currentShift;
    private Double latitude;
    private Double longitude;
    private Double azimuth;
    private Boolean online;
    private UUID activeTripId;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class CurrentShift {
        private UUID id;
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime endDate;
    }
}
