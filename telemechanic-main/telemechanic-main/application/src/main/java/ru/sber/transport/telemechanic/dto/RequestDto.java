package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Schema(title = "Заявка", description = "Данные заявки")
public record RequestDto(
        @Schema(description = "Идентификатор записи о заявке", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Человекочитаемый идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        String humanReadableId,
        @Schema(description = "Автор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        EmployeeDto author,
        @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime creationTime,
        @Schema(description = "Данные автомобиля")
        TransportDto vehicle,
        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        RequestStatus requestStatus,
        @Schema(description = "Проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<CheckDto> checks
) {
}
