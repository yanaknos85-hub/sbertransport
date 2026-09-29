package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "EditDriverRequest", title = "Запрос на редактирование водителя", description = "Запрос на редактирование водителя")
public record EditDriverRequest(
        @Size(min = 10, max = 12)
        @Schema(description = "ИНН",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minLength = 10, maxLength = 12,
                example = "123456789012")
        String tin,
        @Size(min = 11, max = 14)
        @Schema(description = "СНИЛС",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minLength = 11, maxLength = 14,
                example = "112-233-445 95")
        String snils,
        @Schema(description = "Информация о водительском удостоверении",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        DrivingLicenseInfo drivingLicense
) {
}
