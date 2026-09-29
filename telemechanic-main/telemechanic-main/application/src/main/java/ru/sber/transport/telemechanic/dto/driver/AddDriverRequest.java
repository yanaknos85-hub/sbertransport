package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Schema(name = "AddDriverRequest", title = "Запрос на добавление водителя", description = "Запрос на добавление водителя")
public record AddDriverRequest(
        @NotNull
        @Schema(description = "Информация о водителе",
                requiredMode = Schema.RequiredMode.REQUIRED)
        DriverInfo driver,
        @NotNull
        @Schema(description = "Информация о водительском удостоверении",
                requiredMode = Schema.RequiredMode.REQUIRED)
        DrivingLicenseInfo drivingLicense
) {
    
    @Schema(name = "AddDriverRequest.DriverInfo", title = "Информация о водителе", description = "Информация о водителе")
    public record DriverInfo(
            @NotNull
            @Schema(description = "Идентификатор сотрудника",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            UUID employeeId,
            @NotBlank
            @Size(min = 10, max = 12)
            @Schema(description = "ИНН",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    minLength = 10, maxLength = 12,
                    example = "123456789012")
            String tin,
            @NotBlank
            @Size(min = 11, max = 14)
            @Schema(description = "СНИЛС",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    minLength = 11, maxLength = 14,
                    example = "112-233-445 95")
            String snils
    ) {
    }
}
