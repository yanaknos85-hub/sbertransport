package ru.sberbank.ditsib.transport.vehicle.dto.transport.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

@Schema(description = "Данные о дате окончания эксплуацтации")
public record DeactivationDto(
        @Schema(description = "Дата окончания эксплуатации")
        @NotNull(message = "Дата окончания эксплуатации должна быть задана")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime exploitationEnd
) {
}
