package ru.sber.transport.trip.message;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

@Builder
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

        String status,

        UUID ewbUuid
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
