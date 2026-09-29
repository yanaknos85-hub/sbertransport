package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Данные водителя для запроса водителей по ФИО")
public record DriverByFioResponse(
        DriverInfo driver,
        DrivingLicenseInfo drivingLicense
) {

    @Schema(description = "Данные водителя")
    public record DriverInfo(
            @NotNull(message = "Идентификатор записи сотрудника не может отсутствовать")
            @Schema(description = "Идентификатор записи сотрудника",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    format = "uuid",
                    example = "ed518f6f-f2d1-4582-b49b-edc4b5d1959f",
                    minLength = 36, maxLength = 36)
            UUID id,

            @NotBlank(message = "Табельный номер сотрудника не может быть пустым")
            @Schema(description = "Табельный номер водителя",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "21324245",
                    maxLength = 255)
            String personnelNumber,

            @NotBlank(message = "ФИО водителя не может быть пустым")
            @Schema(description = "ФИО водителя",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "Иванов Иван Иванович")
            String fullName,

            @NotBlank(message = "Наименование организации водителя не может быть пустым")
            @Schema(description = "Наименование организации",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "Байкальский Банк",
                    maxLength = 255)
            String organizationName,

            @NotBlank(message = "Наименование подразделения водителя не может быть пустым")
            @Schema(description = "Наименование подразделения",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "КИЦ Волгоградский",
                    maxLength = 255)
            String departmentName,

            @NotBlank(message = "ИНН водителя не может быть пустым")
            @Size(min = 5, max = 12, message = "ИНН должен содержать от 5 до 12 символов")
            @Pattern(regexp = "^\\d{5,12}$", message = "ИНН не прошел проверку")
            @Schema(description = "ИНН",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    example = "111111111111")
            String tin,
            
            /*
              Идентификатор подразделения
             */
            UUID departmentId
    ) {
    }

    @Schema(description = "Водительские права водителя")
    public record DrivingLicenseInfo(
            @NotNull(message = "Идентификатор записи водительских прав водителя не может отсутствовать")
            @Schema(description = "Идентификатор записи водительских прав водителя",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    type = "string",
                    format = "uuid",
                    example = "1e306159-b377-493c-b4a3-5a50575615f7",
                    minLength = 36, maxLength = 36)
            UUID id,
            @NotBlank(message = "Серия водительских прав не может быть пустой")
            @Size(min = 1, max = 20, message = "Серия водительских прав должна быть не менее 1 и не более 20 символов")
            @Schema(description = "Серия водительских прав",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "7708",
                    minimum = "1",
                    maximum = "20")
            String series,

            @NotBlank(message = "Номер водительских прав не может быть пустым")
            @Size(min = 1, max = 20, message = "Номер водительского удостоверения должен быть не менее 1 и не более 20 символов")
            @Schema(description = "Номер водительских прав",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "203040",
                    minimum = "1",
                    maximum = "20")
            String number,

            @NotNull(message = "Дата выдачи водительских прав не может отсутствовать")
            @Schema(description = "Дата выдачи водительских прав",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "2023-01-01")
            LocalDate issueDate
    ) {
    }
}

