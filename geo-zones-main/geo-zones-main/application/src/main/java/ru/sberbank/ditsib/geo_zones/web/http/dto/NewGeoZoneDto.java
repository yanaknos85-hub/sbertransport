package ru.sberbank.ditsib.geo_zones.web.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * New geozone data object.
 */
@Setter(AccessLevel.PROTECTED)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(title = "Объект новой геозоны", description = "Объект с данными новой геозоны или с новыми данными геозоны")
public class NewGeoZoneDto {
    
    /**
     * Name of the geozone.
     */
    @NotBlank
    @Schema(description = "Название геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1)
    private String name;
    
    /**
     * Code of the geozone.
     */
    @Positive
    @NotNull
    @Schema(description = "Код геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer code;
    
    /**
     * Parent of the geozone.
     */
    @JsonProperty("parent_id")
    @Schema(description = "Идентификатор родительской зоны")
    private UUID parentId;

    /**
     * Timezone.
     */
    @JsonProperty("time_zone")
    @Schema(description = "Временная зона")
    private String timeZone;
    
}
