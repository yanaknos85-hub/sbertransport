package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Schema(title = "Данные аттрибутов пользователя по каршерингу (изменение)",
        description = "Измененные данные атрибутов пользователя по каршерингу")
public class CarsharingUIVisibilityDTO {
    @NotNull
    @Schema(description = "ID поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requestIdVisible;
    
    @NotNull
    @Schema(description = "Статус поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requestStatusVisible;
    
    @NotNull
    @Schema(description = "ФИО пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerFioVisible;
    
    @NotNull
    @Schema(description = "Подразделение пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentVisible;
    
    @NotNull
    @Schema(description = "Цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tripPurposeVisible;
    
    @NotNull
    @Schema(description = "Дата и время создания поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean creationTimeVisible;
    
    @NotNull
    @Schema(description = "Дата и время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean desiredDateVisible;
    
    @NotNull
    @Schema(description = "Адрес отправления", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean waypointFromVisible;
    
    @NotNull
    @Schema(description = "Адрес назначения", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean waypointToVisible;
    
    @NotNull
    @Schema(description = "Промежуточные адреса маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean intermediateAddressVisible;
    
    @NotNull
    @Schema(description = "Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengersCountVisible;
    
    @NotNull
    @Schema(description = "Тип поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tripTypeVisible;
    
    @NotNull
    @Schema(description = "Стоимость, руб", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean costVisible;
    
    @NotNull
    @Schema(description = "Фактическая дальность, км", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean actualRangeVisible;
    
    @NotNull
    @Schema(description = "Фактическая длительность, мин", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean actualDurationVisible;
    
    @NotNull
    @Schema(description = "Экономия, руб", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean savingVisible;
    
    @NotNull
    @Schema(description = "ID тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tariffIdVisible;
    
    @NotNull
    @Schema(description = "Перевозчик", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean carrierVisible;
    
    @NotNull
    @Schema(description = "Фактическое время выезда", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean actualDepartureTimeVisible;
    
    @NotNull
    @Schema(description = "ID совместной поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean sharedRideIdVisible;
    
    @NotNull
    @Schema(description = "ID лимита", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean limitIdVisible;
    
    @NotNull
    @Schema(description = "ФИО согласующего", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approvedByFioVisible;
}
