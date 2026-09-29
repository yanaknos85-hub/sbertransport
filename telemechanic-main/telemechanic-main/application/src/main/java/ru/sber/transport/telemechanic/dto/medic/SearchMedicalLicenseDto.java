package ru.sber.transport.telemechanic.dto.medic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

@Schema(title = "Информация о медике", description = "Данные медика")
public record SearchMedicalLicenseDto(
        @NotNull
        @Schema(description = "Идентификатор записи")
        UUID id,
        @NotNull
        @Schema(description = "Наименование организации")
        String organizationName,
        @NotNull
        @Schema(description = "ФИО медика")
        String fullName,
        @NotNull
        @Schema(description = "Должность медика")
        String position,
        @NotNull
        @Schema(description = "Серия лицензии")
        String series,
        @NotBlank
        @Schema(description = "Номер лицензии")
        String number,
        @NotNull
        @Schema(description = "Дата выдачи лицензии")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate issueDate,
        @NotNull
        @Schema(description = "Дата окончания срока действия лицензии")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate expiryDate
) {
}
