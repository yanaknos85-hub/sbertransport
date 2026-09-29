package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;

/**
 * Данные аттрибутов пользователя по общественному транспорту.
 */
@Schema(title = "Данные аттрибутов пользователя по общественному транспорту (изменение)",
        description = "Измененные данные атрибутов пользователя по общественному транспорту")
@Builder
@Jacksonized
public record PublicUIVisibilityDTO(
        @NotNull
        @Schema(description = "ID поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean requestIdVisible,
        @NotNull
        @Schema(description = "Статус поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean requestStatusVisible,
        @NotNull
        @Schema(description = "ФИО пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerFioVisible,
        @NotNull
        @Schema(title = "Цели поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean tripPurposeVisible,
        @NotNull
        @Schema(description = "Дата и время создания поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean creationTimeVisible,
        @NotNull
        @Schema(description = "Дата и время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean desiredDateVisible,
        
        /**
         * Видимость столбца Стоимость, руб
         */
        @NotNull
        @Schema(description = "Стоимость, руб", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean costVisible,
        @NotNull
        @Schema(description = "МВЗ", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean mvzVisible,
        @NotNull
        @Schema(description = "Дата утверждения поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean approveDateVisible,
        @NotNull
        @Schema(description = "Разъездной характер деятельности сотрудника", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean itinerantTypeVisible,
        @NotNull
        @Schema(description = "Тип транспорта маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean transportTypeVisible,
        @NotNull
        @Schema(description = "Вид компенсации", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean compensationTypeVisible,
        @NotNull
        @Schema(description = "Адрес отправления", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean waypointFromVisible,
        @NotNull
        @Schema(description = "Адрес назначения", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean waypointToVisible,
        @NotNull
        @Schema(description = "Промежуточный адрес", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean intermediateAddressVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 1 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentOneVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 2 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentTwoVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 3 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentThreeVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 4 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentFourVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 5 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentFiveVisible,
        @NotNull
        @Schema(description = "Подразделение пассажира 6 лвл", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean passengerDepartmentSixVisible,
        @NotNull
        @Schema(description = "Есть вложение (в заявку вложен билет/картинка)", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasAttachmentVisible,
        @NotNull
        @Schema(description = "Кол-во пунктов маршрута общее", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean waypointsCountVisible,
        @NotNull
        @Schema(description = "Кол-во пунктов маршрута поездки с совпадением координат \"Отметиться\"", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean waypointsCountWithCheckInVisible,
        @NotNull
        @Schema(description = "Кол-во пунктов поездки без совпадения координат \"Отметиться\"", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean waypointsCountWithoutCheckInVisible,
        @NotNull
        @Schema(description = "Период выплаты", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean paymentPeriodVisible,
        @NotNull
        @Schema(description = "Табельный номер пассажира", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean personelNumberVisible,
        @NotNull
        @Schema(description = "Дата утверждения поездки (Дата и время начала формирования приказа на выплату)", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean orderPaymentFormationStartDateVisible,
        @NotNull
        @Schema(description = "Оценка поездки пользователем", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean ratingVisible,
        @NotNull
        @Schema(description = "Комментарий пользователя к оценке", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean ratingCommentVisible,
        @NotNull
        @Schema(description = "Время выплаты", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean paymentTimeVisible

) implements IVisibilityDto {
}
