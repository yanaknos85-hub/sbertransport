package ru.sberbank.ditsib.geo_zones.web.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@ToString
public class WaypointDto {
    
    /**
     * Country.
     */
    @Schema(description = "Страна")
    private final String country;
    
    /**
     * Region.
     */
    @Schema(description = "Регион")
    private final String region;
    
    /**
     * City.
     */
    @Schema(description = "Город")
    private final String city;
    
    /**
     * Street.
     */
    @Schema(description = "Улица")
    private final String street;
    
    /**
     * House.
     */
    @Schema(description = "Дом")
    private final String house;

    /**
     * Район.
     */
    @Schema(description = "Район")
    private final String district;
}
