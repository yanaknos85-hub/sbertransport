package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TelemechOutDto(
        @NotNull
        @Schema(description = "ID автора")
        UUID id,
        
        @NotBlank
        @Schema(description = "Имя")
        String firstName,
        
        @NotBlank
        @Schema(description = "Фамилия")
        String lastName,
        
        @NotBlank
        @Schema(description = "Отчество")
        String patronymic
) {
}
