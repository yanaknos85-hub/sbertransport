package ru.sber.transport.telemechanic.dto.medic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;

import java.time.LocalDate;

@Schema(title = "Редактирование медицинской лицензии", description = "Редактирование медицинской лицензии")
public record EditMedicalLicenseDto(
        @NotBlank
        @Size(min = 1, max = 60)
        @Schema(description = "Серия лицензии")
        String series,
        @NotBlank
        @Size(min = 1, max = 60)
        @Schema(description = "Номер лицензии")
        String number,
        @NotNull
        @Schema(description = "Дата выдачи лицензии")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate issueDate,
        @NotNull
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата окончания срока действия лицензии")
        LocalDate expiryDate
) {
}
