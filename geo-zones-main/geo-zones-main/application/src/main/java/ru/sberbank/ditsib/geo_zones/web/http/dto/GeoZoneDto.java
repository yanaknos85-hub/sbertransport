package ru.sberbank.ditsib.geo_zones.web.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * New geozone data object.
 */
@Builder
@Getter
@Schema(title = "Объект геозоны", description = "Объект с данными геозоны")
public class GeoZoneDto implements HasGeoData {
    
    /**
     * Geozone identifier.
     */
    @NotNull
    @Schema(description = "Идентификтор геозоны", requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID id;
    
    /**
     * Name of the geozone.
     */
    @NotBlank
    @Schema(description = "Название геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1)
    private final String name;
    
    /**
     * Code of the geozone.
     */
    @Positive
    @Schema(description = "Код геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private final String code;
    
    /**
     * Parent of the geozone.
     */
    @JsonProperty("parent_id")
    @Schema(description = "Идентификатор родительской зоны")
    private final UUID parentId;

    @JsonProperty("time_zone")
    @Schema(description = "Временная зона")
    private final String timeZone;
}
