package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Schema(title = "Данные аттрибутов пользователя по такси (изменение)",
        description = "Измененные данные атрибутов пользователя по такси")
public class TaxiUIVisibilityDTO {

    @NotNull
    @Schema(description = "Видимость столбца ID поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requestIdVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Статус поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requestStatusVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца ФИО пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerFioVisible;

    @NotNull
    @Schema(description = "Видимость столбца ID лимита", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean limitIdVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Должность пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerPositionVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца ФИО согласующего", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approvedByFioVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tripPurposeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Места возникноваения затрат для пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerCostCenterVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Разъездной характер работ пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerItinerantTypeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Подразделение пассажира 1 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentOneVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца  Подразделение пассажира 2 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentTwoVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца  Подразделение пассажира 3 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentThreeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Подразделение пассажира 4 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentFourVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Подразделение пассажира 5 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentFiveVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Подразделение пассажира 6 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengerDepartmentSixVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Дата и время создания поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean creationTimeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Адрес отправления", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean waypointFromVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Адрес назначения", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean waypointToVisible;
    
    @NotNull
    @Schema(description = "Видимость промежуточного адреса", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean intermediateAddressVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Общее время ожидания", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean totalWaitingTimeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passengersCountVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца id совместной поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean sharedRideIdVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Тип поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tripTypeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Фактическая стоимость", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tripFactPriceVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Фактическая дальность, км", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean actualRangeVisible;
    
    @NotNull
    @Schema(description = "Видимость столбца Фактическая длительность, мин", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean actualDurationVisible;

    @NotNull
    @Schema(description = "Видимость столбца Экономия, руб", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean savingVisible;

    @NotNull
    @Schema(description = "Видимость столбца ID тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tariffIdVisible;

    @NotNull
    @Schema(description = "Видимость столбца Перевозчик", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean carrierVisible;

    @NotNull
    @Schema(description = "Видимость столбца Комментарий для водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean commentForDriverVisible;

    @NotNull
    @Schema(description = "Видимость столбца Адрес отправления/назначения является адресом ВСП/ГОСБ/ТБ", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean vspGosbTbExistVisible;

    @NotNull
    @Schema(description = "Видимость столбца Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean desiredDateVisible;

    @NotNull
    @Schema(description = "Видимость столбца Дата внесения фактических параметров поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean factParametersSettingTimeVisible;
    
    @NotNull
    @Schema(description = "Оценка поездки пользователем")
    private Boolean ratingVisible;
    
    @NotNull
    @Schema(description = "Комментарий к оценке поездки пользователем")
    private Boolean ratingCommentVisible;
    
    @NotNull
    @Schema(description = "Фактическая дистанция поездки")
    private Boolean taxiTripFactDistance = true;
    
    @NotNull
    @Schema(description = "Фактическая время ожидания водителя")
    private Boolean taxiTripFactWaitTime = true;
    
    @NotNull
    @Schema(description = "HumanReadableID поездки")
    private Boolean taxiTripHumanReadableId = true;
    
    @NotNull
    @Schema(description = "Время окончания поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean finishedTimeVisible;
    
    @NotNull
    @Schema(description = "Контрольный срок подачи ТС")
    private boolean deadlineVisible;
    
    @NotNull
    @Schema(description = "Фактическое время подачи ТС")
    private boolean driverArrivedDatetimeVisible;
    
    @NotNull
    @Schema(description = "Нарушение КС (Да/Нет)")
    private boolean deadlineViolationVisible;
}
