package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Object with data about region.
 */
@Getter
@Builder
@Schema(title = "Регион", description = "Информация по региону")
public class RegionDto {

    /**
     * Code of region.
     */
    @Schema(title = "Код", description = "Код региона")
    private final int code;

    /**
     * Name of region.
     */
    @Schema(title = "Название", description = "Наименование региона")
    private final String name;
    
}
