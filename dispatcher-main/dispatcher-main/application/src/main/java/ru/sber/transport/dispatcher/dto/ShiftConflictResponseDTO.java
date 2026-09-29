package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.dispatcher.database.model.ConflictReason;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Schema(title = "Данные о конфликтах смен для ответа", description = "Данные о конфликтах смен")
public class ShiftConflictResponseDTO {

    @Schema(description = "Идентификатор маршрута")
    private String routeId;

    @Schema(description = "Табельный номер водителя")
    private String personnelNumber;

    @Schema(description = "Государственный номер автомобиля")
    private String stateNumber;

    @Schema(description = "Дата начала конфликта")
    private LocalDateTime startDate;

    @Schema(description = "Дата окончания конфликта")
    private LocalDateTime endDate;

    @Schema(description = "Причина конфликта")
    private ConflictReason conflictReason;

}
