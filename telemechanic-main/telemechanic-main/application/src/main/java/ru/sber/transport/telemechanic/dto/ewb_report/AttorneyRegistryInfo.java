package ru.sber.transport.telemechanic.dto.ewb_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "AttorneyRegistryInfo", title = "Данные об МЧД", description = "Данные об МЧД телемеханика для реестра")
public record AttorneyRegistryInfo(
        @Schema(description = "Номер доверености",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "2f4e5920-3b76-4b0e-2cd0-1bd545ad0989",
                nullable = true)
        UUID number,
        
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @Schema(description = "Дата выдачи доверености",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                type = "integer",
                format = "int64",
                example = "1696616506000",
                nullable = true)
        LocalDateTime issueDate,
        
        @Size(max = 150, message = "Не более 150 символов")
        @Schema(description = "Система создания",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "ООО КОМПАНИЯ",
                nullable = true)
        String creationSystem

) {
}
