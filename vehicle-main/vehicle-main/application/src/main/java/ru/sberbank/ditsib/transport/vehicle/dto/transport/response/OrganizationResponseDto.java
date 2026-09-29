package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Организации")
public record OrganizationResponseDto(
        @Schema(description = "ID организации")
        UUID organizationId,
        @Schema(description = "Название организации")
        String organizationName,
        @Schema(description = "ID департамента")
        UUID departmentId,
        @Schema(description = "Название департамента")
        String departmentName
) {
}
