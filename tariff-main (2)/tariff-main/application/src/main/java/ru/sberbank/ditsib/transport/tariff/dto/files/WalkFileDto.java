package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект с данными пешеходного тарифа.
 */
@Getter
@Setter
public class WalkFileDto implements TariffFileDto {
    
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Организация.
     */
    @NotBlank
    private String organization;
    
    /**
     * Регион.
     */
    @NotBlank
    @NullRender
    private String region;
    
}
