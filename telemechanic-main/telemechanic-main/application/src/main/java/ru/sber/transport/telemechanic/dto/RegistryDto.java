package ru.sber.transport.telemechanic.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @param id                       Идентификатор записи о заявке
 * @param humanReadableId          ID заявки
 * @param officialName             Организация
 * @param creationTime             Дата и время создания заявки
 * @param inspectionTime           Дата и время проведения контроля
 * @param inspectionMark           Отметка о прохождении контроля
 * @param stateNumber              Государственный номер
 * @param personnelNumber          Табельный номер водителя
 * @param fullName                 ФИО водителя
 * @param inspectorPersonnelNumber Табельный номер сотрудника, проводившего контроль
 * @param inspectorFullName        ФИО сотрудника, проводившего контроль
 */
@Schema(title = "Реестр - Телемеханик", description = "Реестр - Телемеханик")
public record RegistryDto(
        @NotNull
        @Schema(description = "Идентификатор записи о заявке", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @NotBlank
        @Schema(description = "ID заявки")
        @Size(max = 100)
        String humanReadableId,
        @NotBlank(message = "Организация должна быть задана")
        @Schema(description = "Организация", requiredMode = Schema.RequiredMode.REQUIRED)
        String officialName,
        @Schema(description = "Дата и время создания заявки")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime creationTime,
        @Schema(description = "Дата и время проведения контроля", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime inspectionTime,
        @Schema(description = "Отметка о прохождении контроля", requiredMode = Schema.RequiredMode.REQUIRED)
        String inspectionMark,
        @NotBlank(message = "Номер должен быть задан")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                message = "Регистрационный знак не прошел проверку")
        @Schema(description = "Государственный номер",
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String stateNumber,
        @NotBlank
        @Size(max = 255)
        @Schema(description = "Марка",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Lada",
                maximum = "255")
        String brand,
        @NotBlank
        @Size(max = 255)
        @Schema(description = "Модель",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Kalina",
                maximum = "255")
        String model,
        @NotBlank(message = "Табельный номер водителя должен быть задан")
        @Schema(description = "Табельный номер водителя", requiredMode = Schema.RequiredMode.REQUIRED)
        String personnelNumber,
        @NotBlank(message = "ФИО водителя должно быть задано")
        @Schema(description = "ФИО водителя", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,
        @NotBlank(message = "Табельный номер сотрудника, проводившего контроль, должен быть задан")
        @Schema(description = "Табельный номер сотрудника, проводившего контроль", requiredMode = Schema.RequiredMode.REQUIRED)
        String inspectorPersonnelNumber,
        @NotBlank(message = "ФИО сотрудника, проводившего контроль, должно быть задано")
        @Schema(description = "ФИО сотрудника, проводившего контроль", requiredMode = Schema.RequiredMode.REQUIRED)
        String inspectorFullName) {
}