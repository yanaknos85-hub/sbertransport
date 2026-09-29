package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Данные диспетчера (ответ от dispatcher-service).
 *
 * @param attorneyNumber номер доверенности
 * @param issueDate дата выдачи доверенности
 * @param expiryDate дата окончания срока действия доверенности
 */
@Schema(title = "Диспетчер", description = "Данные диспетчера от dispatcher-service")
public record DispatcherDto(

        @Schema(title = "Номер доверенности", description = "Номер доверенности сотрудника")
        String attorneyNumber,

        @Schema(title = "Дата выдачи", description = "Дата выдачи доверенности")
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate issueDate,

        @Schema(title = "Дата окончания", description = "Дата окончания срока действия доверенности")
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate expiryDate
) {
}
