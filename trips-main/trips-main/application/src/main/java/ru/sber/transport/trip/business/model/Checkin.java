package ru.sber.transport.trip.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.dto.CheckinType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Checkin {

    private UUID id;

    private UUID tripId;

    private double longitude;

    private double latitude;

    private ZonedDateTime time;

    private String timeZone;

    private TripStatus status;

    private CheckinType type;

}
