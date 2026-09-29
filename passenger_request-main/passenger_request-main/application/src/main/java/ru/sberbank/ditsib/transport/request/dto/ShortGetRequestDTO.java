package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Заявка (Чтение)", description = "Данные заявки")
public class ShortGetRequestDTO {
    
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    @NotNull
    @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripRequestStatus status;
    
    @Schema(description = "Желаемая дата поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;
    
    @NotNull
    @Schema(description = "Временная зона")
    private String timeZone;
    
}
