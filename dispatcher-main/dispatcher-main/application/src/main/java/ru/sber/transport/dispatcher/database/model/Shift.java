package ru.sber.transport.dispatcher.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "dispatcher", name = "shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "contractor_id")
    private UUID contractorId;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "active")
    private boolean active = false;

    @Column(name = "row_id")
    private UUID rowId;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "ewb_id")
    private UUID ewbId;
}
