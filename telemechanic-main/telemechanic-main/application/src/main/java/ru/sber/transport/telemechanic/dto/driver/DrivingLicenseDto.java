package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Водительские права водителя")
public record DrivingLicenseDto(
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
        LocalDate issueDate,
        
        @NotNull(message = "Дата истечения водительских прав не может отсутствовать")
        @Schema(description = "Дата истечения водительских прав",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2024-01-01")
        LocalDate expiryDate,
        
        @Schema(description = "Статус",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "true")
        boolean active,
        
        @NotEmpty(message = "Набор категорий водительских прав не может быть пустым")
        @Schema(description = "Набор категорий, которые открыты в водительских правах",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[A, B]")
        Set<String> categoryNames
) {
}
