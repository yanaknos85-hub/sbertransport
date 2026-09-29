package ru.sber.transport.trips.cargo.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripHistoryItem {

    private LocalDateTime changeTime;

    private UUID tripId;

    private UUID oldDispatcherId;

    private UUID newDispatcherId;

    private UUID oldDriverId;

    private UUID newDriverId;

    private TripStatus oldStatus;

    private TripStatus newStatus;

    private UUID actorId;

    private Actor actorType;

    private ActionType action;

    private UUID oldPlannedShiftId;

    private UUID newPlannedShiftId;
}
