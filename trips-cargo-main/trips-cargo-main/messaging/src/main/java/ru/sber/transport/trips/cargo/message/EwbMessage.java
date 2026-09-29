package ru.sber.transport.trips.cargo.message;

import jakarta.validation.constraints.NotNull;
import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

public record EwbMessage(

        @NotNull
        UUID id,

        @NotNull
        String humanReadableId,

        @NotNull
        LocalDate ewbStartDate,

        @NotNull
        LocalDate ewbFinishDate,

        @NotNull
        UUID organizationId,

        @NotNull
        UUID transportId,

        @NotNull
        UUID driverEmployeeId,

        @NotNull
        Integer odometerOut,

        @NotNull
        Integer odometerIn,

        @NotNull
        Integer fuelLitreageOut,

        @NotNull
        Integer fuelLitreageIn,

        String status
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
