package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Краткая информация о водителе.
 *
 * @param id идентификатор.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param contractorId контрагент.
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param contactPhone контактный телефон.
 * @param rating рейтинг.
 * @param active признак активности.
 */
@Builder
@Schema(title = "Информация о водителе", description = "Краткие данные водителя")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DriverShortDTO (

        @NotNull
        @Schema(description = "Идентификатор")
        UUID id,

        @NotNull
        @Schema(description = "Человекочитаемый идентификатор")
        String humanReadableId,

        @Schema(description = "Контрагент")
        UUID contractorId,

        @Schema(description = "Фамилия")
        String lastName,

        @Schema(description = "Имя")
        String firstName,

        @Schema(description = "Отчество")
        String patronymic,

        @Schema(description = "Контактный номер")
        String contactPhone,

        @Schema(description = "Рейтинг", defaultValue = "500")
        Integer rating,

        @Schema(description = "Активность")
        boolean active,

        @Schema(description = "ID смены")
        UUID shiftId

) {}

