package ru.sber.transport.request_checks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;

@Tag(
    name = "API для проверки длительности поездок",
    description = "Проверка лимита длительности поездок (не более 12 часов в сутки)"
)
@Validated
@RequestMapping("/duration")
public interface DurationRequestCheckController {

    @Operation(summary = "Проверка лимита длительности поездки",
        description = "Проверка, что сумма длительности всех поездок за день + новая поездка не превышает 12 часов.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для проверки длительности поездки",
            required = true,
            content = @Content(schema = @Schema(implementation = DurationCheckRequestDto.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешная проверка"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные в запросе"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Превышена продолжительность поездок в 12 часов в сутки"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping
    void checkDurationLimit(
        @Valid @RequestBody DurationCheckRequestDto request,
        @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

}
