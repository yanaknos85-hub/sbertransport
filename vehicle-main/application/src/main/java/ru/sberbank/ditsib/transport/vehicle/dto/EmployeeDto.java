package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDto(
        
        @NotNull
        @Schema(description = "Идентификатор")
        UUID id,
        
        @Schema(description = "Табельный номер")
        String personnelNumber,
        
        @Schema(description = "ФИО")
        String fullName,
        
        @Schema(description = "ID организации")
        UUID organizationId,
        
        @Schema(description = "Организация")
        String organizationName,
        
        @Schema(description = "ID подразделения")
        UUID departmentId,
        
        @Schema(description = "Подразделение")
        String departmentName
) {
}
