package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;
import lombok.*;

import jakarta.validation.constraints.*;
import java.util.*;

/**
 * Краткое инфо о диспетчере.
 *
 * @param id идентификатор.
 * @param contractorId контрагент.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param contactPhone контактный телефон.
 */
@Builder
@Schema(title = "Информация о диспетчере", description = "Данные диспетчера")
public record DispatcherShortDTO(

    @NotNull
    @Schema(description = "Идентификатор")
    UUID id,

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

    @Schema(description = "Человекочитаемый идентификатор")
    String humanReadableId

){}
