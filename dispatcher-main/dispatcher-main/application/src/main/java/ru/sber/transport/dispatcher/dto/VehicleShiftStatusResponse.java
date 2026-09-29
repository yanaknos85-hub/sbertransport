package ru.sber.transport.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleShiftStatusResponse {

    private UUID vehicleId;

    private boolean online;

    private UUID driverId;

}
