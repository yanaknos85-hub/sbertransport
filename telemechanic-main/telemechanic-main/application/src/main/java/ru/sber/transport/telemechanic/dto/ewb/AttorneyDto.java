package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AttorneyDto(
        @NotNull
        @Schema(description = "ID доверенности")
        UUID id,
        
        @NotNull
        @Schema(description = "Номер доверенности")
        UUID attorneyNumber,
        
        @NotNull
        @Schema(description = "Дата доверенности")
        LocalDate issueDate,
        
        @NotNull
        @Schema(description = "Дата окончания срока действия")
        LocalDate expiryDate,
        
        @NotBlank
        @Schema(description = "Система, в которой осуществляется хранение доверенности")
        String creationSystem
) {
}
