package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(title = "Точки поездки", description = "Узловые точки")
@AllArgsConstructor
@NoArgsConstructor
public class WaypointDTO {

    @Schema(description = "Страна")
    private String country;

    @Schema(description = "Регион")
    private String region;

    @Schema(description = "Город")
    private String city;

    @Schema(description = "Улица")
    private String street;

    @Schema(description = "Дом")
    private String house;

    @Schema(description = "Строение")
    private String building;

    @Schema(description = "Корпус")
    private String structure;

    @Schema(description = "Автоматический чекин")
    private Boolean checkinAutomatic = false;

    @Schema(description = "Ручной чекин")
    private Boolean checkinManual = false;

    @Schema(description = "Время ожидания")
    private int waitTime;

    @Schema(description = "Адрес найден в реестре ВСП/ГОСБ/ТБ")
    private Boolean existInVspGosbTbRegistry;
}
