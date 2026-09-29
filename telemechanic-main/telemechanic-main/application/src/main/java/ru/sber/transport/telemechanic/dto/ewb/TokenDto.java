package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "Токен авторизации в системе КОРУС")
public record TokenDto(
        
        @NotNull
        @NotBlank
        @Schema(description = "Токен авторизации")
        String token
) {
}
