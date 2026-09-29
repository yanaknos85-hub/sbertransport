package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.*;

import java.time.LocalDate;
import java.util.*;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY;

/**
 * Данные диспетчера.
 *
 * @param id идентификатор.
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param phone номер телефона.
 * @param email E-Mail.
 * @param contractorId идентификатор контрагента.
 */
@Schema(title = "Диспетчер",
        description = "Данные диспетчера")
public record DispatcherDto(

        @Schema(title = "Идентификатор")
        UUID id,

        @Schema(title = "Фамилия")
        String lastName,

        @Schema(title = "Имя")
        String firstName,

        @Schema(title = "Отчество")
        String patronymic,

        @Schema(title = "Человекочитаемый идентификатор")
        String humanReadableId,

        @Schema(title = "Номер телефона", pattern = "(\\+7|8)\\d{10}")
        String phone,

        @Schema(title = "E-Mail", pattern = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[\\w-]+\\.)+\\w{2,6}$")
        String email,

        @Schema(title = "ID контрагента")
        UUID contractorId,

        @Schema(description = "Признак автоназначения")
        boolean autoassign,

        @Schema(title = "Подписание ПДн")
        Boolean consent,

        @Schema(title = "Подтверждение номера телефона")
        boolean phoneConfirmed,

        @Schema(title = "Идентификатор филиала")
        UUID autoparkId,

        @Schema(title = "Идентификатор организации")
        String autoparkName,

        @Schema(title = "Идентификатор пользователя во внешней системе")
        UUID oauthId,

        @Schema(description = "Флаг возможности создания ЭПЛ")
        boolean ewbCreationPossibility,

        @Schema(description = "Табельный номер")
        String personnelNumber,

        @Schema(description = "Номер доверенности")
        String attorneyNumber,

        @Schema(description = "Дата выдачи")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate issueDate,

        @Schema(description = "Дата окончания срока действия")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate expiryDate,

        @Schema(description = "Система создания")
        String creationSystem

) implements HasName {
}
