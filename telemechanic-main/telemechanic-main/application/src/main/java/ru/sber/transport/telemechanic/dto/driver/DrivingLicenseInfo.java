package ru.sber.transport.telemechanic.dto.driver;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(name = "DrivingLicenseInfo", title = "Информация о водительском удостоверении", description = "Информация о водительском удостоверении")
public record DrivingLicenseInfo(
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
                example = "7708",
                minimum = "1",
                maximum = "20")
        String number,
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @NotNull
        @PastOrPresent(message = "Дата выдачи ВУ должна быть меньше или равна текущей дате")
        @Schema(description = "Дата выдачи",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2023-01-01")
        LocalDate issueDate,
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @NotNull
        @FutureOrPresent(message = "Дата окончания срока действия  ВУ должна быть больше или равна текущей дате")
        @Schema(description = "Дата окончания срока действия",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2024-01-01")
        LocalDate expiryDate,
        @NotEmpty
        @Schema(description = "Категории водительского удостоверения",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Set<UUID> categoryIds
) {
}
