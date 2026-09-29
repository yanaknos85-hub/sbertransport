package ru.sber.transport.telemechanic.dto.dispatcher;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "GetDispatcherResponse", title = "Ответ на запрос информации о диспетчере")
public record GetDispatcherResponse(
        @Schema(description = "Идентификатор диспетчера",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Информация о диспетчере",
                requiredMode = Schema.RequiredMode.REQUIRED)
        DispatcherInfo dispatcher,
        @Schema(description = "Флаг активности диспетчера",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean active,
        @Schema(description = "Номер доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID attorneyNumber,
        @JsonSerialize(using = LocalDateSerializer.class)
        @Schema(description = "Дата выдачи доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2020-01-01")
        LocalDate issueDate,
        @JsonSerialize(using = LocalDateSerializer.class)
        @Schema(description = "Дата окончания срока действия доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2030-01-01")
        LocalDate expiryDate,
        @Schema(description = "Система создания доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "АС-СберТранспорт")
        String creationSystem
) {
}
