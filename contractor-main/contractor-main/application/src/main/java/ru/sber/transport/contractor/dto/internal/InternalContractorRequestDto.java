package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Setter;

import java.util.UUID;


/**
 * Объект с новыми данными контрагента.
 *
 * @param name           название.
 * @param mainDispatcher данные основного диспетчера.
 */
@Schema(title = "Информация о контрагенте", description = "Новые данные контрагента")
@Builder
public record InternalContractorRequestDto(

        @NotBlank
        @Schema(description = "Полное наименование", maxLength = 128)
        @Size(max = 128)
        String name,

        @NotNull
        @Schema(description = "ИНН", minLength = 10, maxLength = 12)
        @Size(min = 10, max = 12)
        String tin,

        @NotNull
        @Pattern(regexp = "\\d{13}", message = "ОРГН должен состоять из 13 чисел")
        @Schema(description = "ОРГН, 13 знаков")
        String msrn,

        @NotNull
        @Schema(description = "Информация об основном диспетчере. Будет заведен новый. Ниже по приоритету, нежели " +
                "`mainDispatcherId`. Если диспетчер с идентификатором не найден, но предоставлены сведения о новом " +
                "диспетчере, будет заведен новый диспетчер")
        NewDispatcherDto mainDispatcher,

        @Schema(title = "E-Mail")
        String technicalAccountOwnerEmail,

        @Schema(description = "ФИО владельца ТУЗ")
        String technicalAccountOwner,

        @Schema(description = "Тип интеграции")
        String integrationType,

        @Schema(description = "Логин ТУЗ")
        String technicalAccountLogin,

        @Setter
        @Schema(description = "Пароль ТУЗ")
        String technicalAccountPassword,

        @Schema(description = "Нормативное количество автомобилей в автопарке")
        Integer vehicleCountNorm,

        @Schema(description = "Признак внутреннего автопарка")
        boolean isInternal

) {
    /**
     * Новые данные диспетчера.
     *
     * @param lastName   фамилия.
     * @param firstName  имя.
     * @param patronymic отчество.
     * @param phone      номер телефона.
     * @param email      E-Mail.
     */
    @Schema(title = "Диспетчер",
            description = "Новые данные диспетчера")
    @Builder
    public record NewDispatcherDto(

            @Schema(title = "ID")
            UUID id,

            @NotBlank
            @Schema(title = "Фамилия")
            @Pattern(regexp = "^[a-zA-ZА-ЯЁа-яё]{1,50}$")
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
            String phone,

            @NotBlank
            String email,

            @Schema(description = "Идентификатор пользователя в СберТранспорт")
            UUID oauthId
    ) {
    }
}
