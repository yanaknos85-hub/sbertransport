package ru.sber.transport.dispatcher.dto.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(title = "Данные для регистрации ТУЗ",
        description = "Данные для регистрации ТУЗ")
@Builder
public record RegistrationDataDto(

        @NotBlank
        @Email
        @Schema(title = "Почта", description = "Почта")
        String email,

        @NotBlank
        @Schema(title = "Логин", description = "Логин")
        String login,

        @NotBlank
        @Size(min = 6, max = 20)
        @Schema(title = "Пароль", description = "Пароль", minLength = 6, maxLength = 20)
        String password,

        @NotBlank
        @Schema(title = "Область видимости", description = "Область видимости")
        String scope
) {
}
