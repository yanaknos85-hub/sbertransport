package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDTO {

    @Schema(description = "id")
    private UUID id;

    @Schema(description = "brand")
    private String brand;

    @Schema(description = "model")
    private String model;

    @Schema(description = "stateNumber")
    private String stateNumber;

    @Schema(description = "color")
    private String color;

    @Schema(description = "autoparkId")
    private UUID autoparkId;

    @Schema(description = "deleted")
    private boolean deleted = false;
}
