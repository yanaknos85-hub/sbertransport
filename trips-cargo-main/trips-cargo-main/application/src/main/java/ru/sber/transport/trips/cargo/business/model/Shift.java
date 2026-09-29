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
public class Shift {

    private UUID id;

    private UUID contractorId;

    private UUID driverId;

    private UUID vehicleId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean deleted;

    private boolean active;

    private UUID ewbId;

}
