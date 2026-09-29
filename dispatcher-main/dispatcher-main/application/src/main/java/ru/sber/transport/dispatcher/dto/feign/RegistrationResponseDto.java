package ru.sber.transport.dispatcher.dto.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Schema(title = "Данные регистрации ТУЗ",
        description = "Данные регистрации ТУЗ")
@Builder
public record RegistrationResponseDto(

        @Schema(title = "Id пользователя", description = "Id пользователя")
        @NotNull
        UUID userId
) {
}
