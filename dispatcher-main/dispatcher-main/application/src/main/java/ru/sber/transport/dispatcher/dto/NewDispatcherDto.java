package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.*;
import io.swagger.v3.oas.annotations.media.*;
import ru.sber.transport.dispatcher.serde.PhoneSerializer;

import jakarta.validation.constraints.*;
import ru.sber.transport.dispatcher.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.UUID;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY;

/**
 * Новые данные диспетчера.
 *
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param phone номер телефона.
 * @param email E-Mail.
 */
@Schema(title = "Диспетчер",
        description = "Новые данные диспетчера")
public record NewDispatcherDto(

        @Schema(title = "ID")
        UUID id,

        @NotBlank
        @Schema(title = "Фамилия")
        @Pattern(regexp = "^[a-zA-ZА-ЯЁа-яё\\-]{1,50}$")
        String lastName,

        @NotBlank
        @Schema(title = "Имя")
        @Pattern(regexp = "^[a-zA-ZА-ЯЁа-яё]+$")
        String firstName,

        @Schema(title = "Отчество")
        @Pattern(regexp = "^[-a-zA-ZА-ЯЁа-яё\\s]+$")
        String patronymic,

        @NotBlank
        @Pattern(regexp = "(\\+7|8)\\d{10}")
        @Schema(title = "Номер телефона", pattern = "(\\+7|8)\\d{10}")
        @JsonDeserialize(using = PhoneSerializer.class)
        String phone,

        @NotBlank
        @Email(regexp = ValidationConstants.EMAIL_REGEX)
        @Schema(title = "E-Mail", pattern = ValidationConstants.EMAIL_REGEX)
        String email,

        @Schema(title = "Иденификатор пользователя в СберТранспорт")
        UUID oauthId,

        @Schema(description = "Идентификатор филиала")
        UUID autoparkId,

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
) {
}
