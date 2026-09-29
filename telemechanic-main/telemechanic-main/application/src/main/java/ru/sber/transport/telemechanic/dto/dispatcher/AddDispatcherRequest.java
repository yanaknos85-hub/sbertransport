package ru.sber.transport.telemechanic.dto.dispatcher;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "AddDispatcherRequest", title = "Данные для добавление диспетчера")
public record AddDispatcherRequest(
        @NotNull
        @Schema(description = "Идентификатор диспетчера",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID employeeId,
        @NotNull
        @Schema(description = "Идентификатор организации владельца автопарка",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID organizationId,
        @NotNull
        @Schema(description = "Идентификатор подразделения владельца автопарка",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID departmentId,
        @NotNull
        @Schema(description = "Номер доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID attorneyNumber,
        @NotNull
        @PastOrPresent(message = "Дата выдачи доверенности должна быть меньше или равна текущей дате")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата выдачи доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate issueDate,
        @NotNull
        @FutureOrPresent(message = "Дата окончания срока действия  доверенности должна быть больше или равна текущей дате")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата окончания срока действия доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate expiryDate,
        @NotBlank
        @Size(max = 150)
        @Schema(description = "Система создания доверенности",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "КОРУС",
                maximum = "150")
        String creationSystem
) {
}
