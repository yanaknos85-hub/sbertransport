package ru.sberbank.ditsib.geo_zones.web.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public interface HasGeoData {

    @Schema(description = "Идентификтор геозоны", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID getId();

    @Schema(description = "Название геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1)
    String getName();

    @Schema(description = "Код геозоны", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    String getCode();

    @Schema(description = "Идентификатор родительской зоны")
    UUID getParentId();

    @Schema(description = "Временная зона")
    String getTimeZone();

}
