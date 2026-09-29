package ru.sber.transport.telemechanic.dto.ewb_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;

@Schema(name = "DrivingLicenseRegistryInfo", title = "Данные для реестра по водительскому удостоверению",
        description = "Данные по водительскому удостоверению для ответа формирования реестра ЭПЛ")
public record DrivingLicenseRegistryInfo(
        @Size(min = 1, max = 20, message = "Серия водительских прав должна быть не менее 1 и не более 20 символов")
        @Schema(description = "Серия водительского удостоверения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "7708",
                minimum = "1",
                maximum = "20",
                nullable = true)
        String series,
        
        @Size(min = 1, max = 20, message = "Номер водительского удостоверения должен быть не менее 1 и не более 20 символов")
        @Schema(description = "Номер водительского удостоверения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "203040",
                minimum = "1",
                maximum = "20",
                nullable = true)
        String number,
        
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата выдачи водительского удостоверения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "2023-09-28",
                nullable = true)
        LocalDate issueDate,
        
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата окончания водительского удостоверения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "2025-02-26",
                nullable = true)
        LocalDate expiryDate
) {
}
