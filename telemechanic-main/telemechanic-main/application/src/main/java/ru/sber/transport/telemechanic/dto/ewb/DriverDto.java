package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DriverDto(
        @NotNull
        @Schema(description = "ID водителя")
        UUID id,
        
        @NotBlank
        @Schema(description = "Имя")
        String firstName,
        
        @NotBlank
        @Schema(description = "Фамилия")
        String lastName,
        
        @Schema(description = "Отчество")
        String patronymic,
        
        @NotBlank
        @Schema(description = "Таб.номер водителя")
        String personnelNumber,
        
        @NotBlank
        @Schema(description = "Организация")
        String organizationName,
        
        @NotBlank
        @Schema(description = "Подразделение")
        String departmentName,
        
        @NotBlank
        @Schema(description = "ИНН водителя")
        String tin
) {
}
