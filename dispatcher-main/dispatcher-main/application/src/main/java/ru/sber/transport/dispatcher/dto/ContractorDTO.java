package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;

import jakarta.validation.constraints.*;
import java.util.*;

/**
 * Объект с данными контрагента.
 *
 * @param name название.
 * @param digitId номер.
 * @param id идентификатор.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param mainDispatcher данные основного диспетчера.
 */
@Schema(title = "Информация о контрагенте", description = "Данные контрагента")
public record ContractorDTO(

        @NotBlank
        @Schema(description = "Полное наименование")
        @Size(max = 128)
        String name,

        @NotNull
        @Schema(description = "ИНН", minLength = 10, maxLength = 12)
        @Size(min = 10, max = 12)
        String tin,

        @Schema(description = "человекочитаемый id")
        Long digitId,

        @NotNull
        @Schema(description = "Идентификатор")
        UUID id,

        @NotNull
        @Schema(description = "Человекочитаемый Идентификатор")
        String humanReadableId,

        @Schema(description = "Информация об основном диспетчере")
        DispatcherDto mainDispatcher,

        @Schema(description = "Флаг включенности автоназначения")
        boolean autoassign,

        @Schema(description = "Электронная почта владельца ТУЗ")
        String technicalAccountOwnerEmail,

        @Schema(description = "ФИО владельца ТУЗ")
        String technicalAccountOwner,

        @Schema(description = "Признак внутреннего автопарка")
        boolean isInternal

) { }