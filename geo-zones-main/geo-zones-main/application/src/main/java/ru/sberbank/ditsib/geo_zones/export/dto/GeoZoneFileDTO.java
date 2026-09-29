package ru.sberbank.ditsib.geo_zones.export.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.spreadsheet.annotation.NullRender;

/**
 * Объект геозоны в файле
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeoZoneFileDTO {
    
    /**
     * Код геозоны.
     */
    @NullRender
    @NotNull
    private String code;
    
    /**
     * Название геозоны.
     */
    @NotBlank
    private String name;
    
    /**
     * Имя родительской зоны
     */
    private String parentName;
    
    /**
     * Идентификатор родительской зоны
     */
    private String parentCode;

    /**
     * Временная зона
     */
    private String timeZone;
    
    
}
