package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.sber.transport.dispatcher.validation.ValidationConstants;


/**
 * Объект с новыми данными контрагента.
 *
 * @param name           название.
 * @param mainDispatcher данные основного диспетчера.
 */
@Schema(title = "Информация о контрагенте", description = "Новые данные контрагента")
public record NewContractorDTO(

        @NotBlank
        @Schema(description = "Полное наименование", maxLength = 128)
        @Size(max = 128)
        String name,

        @NotNull
        @Schema(description = "ИНН", minLength = 10, maxLength = 12)
        @Size(min = 10, max = 12)
        String tin,

        @NotNull
        @Pattern(regexp = "^(\\d{13}|\\d{15})$", message = "ОГРН должен состоять из 13 или 15 чисел")
        @Schema(description = "ОГРН, 13 знаков", pattern = "^(\\d{13}|\\d{15})$", minLength = 13, maxLength = 15)
        String msrn,

        @NotNull
        @Schema(description = "Информация об основном диспетчере. Будет заведен новый. Ниже по приоритету, нежели " +
                "`mainDispatcherId`. Если диспетчер с идентификатором не найден, но предоставлены сведения о новом " +
                "диспетчере, будет заведен новый диспетчер")
        NewDispatcherDto mainDispatcher,

        @Email(regexp = ValidationConstants.EMAIL_REGEX)
        @Schema(title = "E-Mail", pattern = ValidationConstants.EMAIL_REGEX)
        String technicalAccountOwnerEmail,

        @Schema(description = "ФИО владельца ТУЗ")
        String technicalAccountOwner,

        @Schema(description = "Тип интеграции")
        IntegrationTypeDto integrationType,

        @Schema(description = "Логин ТУЗ")
        @NotNull
        String technicalAccountLogin,

        @Schema(description = "Пароль ТУЗ")
        @NotBlank
        String technicalAccountPassword,

        @Schema(description = "Нормативное количество автомобилей в автопарке")
        Integer vehicleCountNorm,

        @Schema(description = "Признак внутреннего автопарка")
        boolean isInternal

) {
}
