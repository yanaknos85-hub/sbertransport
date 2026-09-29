package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;

/**
 * Объект обмена данными для интеграции через API.
 *
 * @param url адрес для обмена данными
 * @param login логин
 * @param password пароль
 */
@Schema(title = "Интеграционные параметры")
public record JsonIntegrationParamsDto (

    @NotBlank
    @Schema(description = "Адрес для обмена данными", minLength = 1)
    String url,

    @NotBlank
    @Schema(description = "Логин", minLength = 1)
    String login,

    @NotBlank
    @Schema(description = "Пароль", minLength = 1)
    String password
) {}
