package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public record EmployeeDTO(
        @NotNull
        @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @Schema(description = "Имя")
        String firstName,
        
        @Schema(description = "Фамилия")
        String lastName,
        
        @Schema(description = "Отчество")
        String patronymic,
        
        @Schema(description = "Табельный номер")
        String personnelNumber,
        
        @Schema(description = "Идентификатор подразделения")
        UUID departmentId,
        
        @Schema(description = "Идентификатор пользователя")
        UUID userId,
        
        @Schema(description = "Идентификатор должности")
        UUID positionId,
        
        @Schema(description = "Делегатор")
        UUID delegatedById,
        
        @Schema(description = "Название должности")
        String positionName,
        
        @Schema(description = "Рук-ль")
        UUID supervisorId,
        
        @Schema(description = "Организация")
        UUID organizationId,
        
        @Schema(description = "Название организации")
        String organizationName,
        
        @Schema(description = "Название подразделения")
        String departmentName,
        
        @Schema(description = "Место возникновения затрат")
        String mvz,
        
        @Schema(description = "Человекочитаемый идентификатор")
        String humanReadableId,
        
        @Schema(description = "Номер телефона")
        String mobilePhone

) {
    
}