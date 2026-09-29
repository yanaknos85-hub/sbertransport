package ru.sberbank.ditsib.geo_zones.web.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

/**
 * New geozone data object.
 */
@Builder
@Getter
@Schema(title = "Объект геозоны", description = "Объект с данными геозоны")
public class GeoZoneWithChildrenDto implements HasGeoData, HasChildren {
    
    /**
     * Geozone identifier.
     */
    @NotNull
    private final UUID id;
    
    /**
     * Name of the geozone.
     */
    @NotBlank
    private final String name;
    
    /**
     * Code of the geozone.
     */
    @Positive
    private final String code;
    
    /**
     * Parent of the geozone.
     */
    @JsonProperty("parent_id")
    private final UUID parentId;
    
    /**
     * Parent of the geozone.
     */
    @JsonProperty("parent_code")
    @Schema(description = "Идентификатор родительской зоны")
    private final String parentCode;

    @JsonProperty("time_zone")
    @Schema(description = "Временная зона")
    private final String timeZone;
    
    private final List<GeoZoneWithChildrenDto> children;
    
}
