package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;

/**
 * Данные аттрибутов пользователя по личному транспорту
 */
@Schema(title = "Данные аттрибутов пользователя по личному транспорту (изменение)",
        description = "Измененные данные атрибутов пользователя по личному транспорту")
@Builder
@Jacksonized
public record PersonalUIVisibilityDTO(
        @NotNull
        @Schema(description = "ID поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean requestIdVisible,
        
        @NotNull
        @Schema(description = "Статус поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean requestStatusVisible,
        
        @NotNull
        @Schema(description = "ФИО польз. К.К.", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerFioVisible,
        
        @NotNull
        @Schema(description = "Фактическая стоимость", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean tripFactPriceVisible,
        
        @NotNull
        @Schema(description = "МВЗ", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean mvzVisible,
        
        @NotNull
        @Schema(description = "Расстояние поездки, км", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean actualRangeVisible,
        
        @NotNull
        @Schema(description = "Табельный номер польз. К.К", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean kkPersonalNumberVisible,
        
        @NotNull
        @Schema(description = "Право собственности на автомобиль", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean ownershipOfCarVisible,
        
        @NotNull
        @Schema(description = "Объем двигателя автомобиля", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean carEngineVolumeVisible,
        
        @NotNull
        @Schema(description = "Период выплаты", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean paymentPeriodVisible,
        
        @NotNull
        @Schema(description = "Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean desiredDateVisible,
        
        @NotNull
        @Schema(description = "Дата утверждения поездки (Дата и время начала формирования приказа на выплату)", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean orderPaymentFormationStartDateVisible,
        
        @NotNull
        @Schema(description = "Видимость столбца paymentCost - Сумма к выплате", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean paymentCostVisible,
        
        @NotNull
        @Schema(description = "Видимость столбца sharedRideOwner - Признак водитель/пассажир", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean sharedRideOwnerVisible

) implements IVisibilityDto {
}
