package ru.sber.transport.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Информация о сотруднике
 *
 * @param id Идентификатор записи о сотруднике
 * @param fullName ФИО
 * @param mobilePhone Номер телефона
 * @param userId Идентификатор пользователя
 */
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDto(
        @Schema(description = "Идентификатор записи о сотруднике", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID id,
        @Schema(description = "ФИО", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String fullName,
        @Schema(description = "Номер телефона", nullable = true, maxLength = 255)
        @Size(max = 255)
        String mobilePhone,
        @Schema(description = "Идентификатор пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID userId
) {
}