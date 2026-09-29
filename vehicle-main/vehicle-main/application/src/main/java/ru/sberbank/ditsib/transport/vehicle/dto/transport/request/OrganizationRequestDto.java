package ru.sberbank.ditsib.transport.vehicle.dto.transport.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.With;

import java.util.UUID;

@Schema(description = "Организации")
@With
public record OrganizationRequestDto(
        @Schema(description = "ID организации", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID организации должен быть задан")
        UUID organizationId,
        @Schema(description = "ID департамента", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID департамента должен быть задан")
        UUID departmentId
) {
}
