package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Coordinates
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Coordinates implements Serializable {
    @JsonProperty("latitude")
    @Schema(description = "Широта")
    private Double latitude;

    @JsonProperty("longitude")
    @Schema(description = "Долгота")
    private Double longitude;
}
