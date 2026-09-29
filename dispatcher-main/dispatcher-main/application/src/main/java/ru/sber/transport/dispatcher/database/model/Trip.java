package ru.sber.transport.dispatcher.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "dispatcher", name = "trips")
public class Trip {

    @Id
    private UUID id;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "contractor_id")
    private UUID contractorId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;
}
