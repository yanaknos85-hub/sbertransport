package ru.sberbank.ditsib.transport.vehicle.dto.attorney;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelemechanicDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Данные МЧД")
public record AttorneyDto(

        @NotNull
        @Schema(description = "Идентификатор записи МЧД", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotNull
        @Schema(description = "Данные телемеханика", requiredMode = Schema.RequiredMode.REQUIRED)
        TelemechanicDto telemechanic,

        @NotNull
        @Schema(description = "Номер доверенности", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID attorneyId,

        @NotNull
        @Schema(description = "Дата выдачи", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime issueDate,

        @NotNull
        @Schema(description = "Дата окончания срока действия", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime expiryDate,

        @NotBlank
        @Schema(description = "Система создания", requiredMode = Schema.RequiredMode.REQUIRED)
        String creationSystem

) {
}
