package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(title = "Информация о подразделении", description = "Данные подразделения")
public record DepartmentWithAutoparkDto(
        @Schema(description = "Идентификатор подразделения")
        UUID id,
        
        @Schema(description = "Наименование филиала")
        String autoparkName
) {

}
