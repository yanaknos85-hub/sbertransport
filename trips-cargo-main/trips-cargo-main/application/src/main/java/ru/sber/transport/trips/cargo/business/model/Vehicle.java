package ru.sber.transport.trips.cargo.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    private UUID id;

    private String brand;

    private String model;

    private String stateNumber;

    private String color;

    private UUID contractorId;

    private boolean deleted;

    private UUID autoparkId;
}
