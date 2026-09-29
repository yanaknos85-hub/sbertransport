package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Object with data about waypoint.
 */
@Getter
@Setter
@Builder
@Schema(title = "Точки поездки", description = "Узловые точки")
@AllArgsConstructor
@NoArgsConstructor
public class WaypointDTO {
    
    
    /**
     * Country.
     */
    @Schema(description = "Страна")
    private String country;
    
    /**
     * Region.
     */
    @Schema(description = "Регион")
    private String region;
    
    /**
     * City.
     */
    @Schema(description = "Город")
    private String city;
    
    /**
     * Street.
     */
    @Schema(description = "Улица")
    private String street;
    
    /**
     * House.
     */
    @Schema(description = "Дом")
    private String house;
    
    /**
     * Building.
     */
    @Schema(description = "Строение")
    private String building;
    
    /**
     * Structure.
     */
    @Schema(description = "Корпус")
    private String structure;

    /**
     * Автоматический чекин
     */
    @Schema(description = "Автоматический чекин")
    @Builder.Default
    private Boolean checkinAutomatic = false;

    /**
     * Ручной чекин
     */
    @Schema(description = "Ручной чекин")
    @Builder.Default
    private Boolean checkinManual = false;

    /**
     * Address from VSP/GOSB/TB registry
     */
    @Schema(description = "Адрес найден в реестре ВСП/ГОСБ/ТБ")
    private Boolean existInVspGosbTbRegistry;
    
    /**
     * Address from cargo-2
     */
    @Schema(description = "Строковое представление адреса")
    private String addressString;

}
