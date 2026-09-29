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
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;

@Tag(
        name = "API для проверки суммарного километража",
        description = "Проверка лимита по всем заявкам за текущий календарный месяц для пассажира указанного в заявке"
)
@Validated
@RequestMapping("/distance")
public interface OverrunRequestCheckController {

    @Operation(summary = "Проверка суммарного километража",
            description = "Проверка, что сумма пробега за месяц на пассажира меньше порогового значения",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для проверки суммарного километража",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OverrunCheckRequestDto.class))
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
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    @PostMapping("/total")
    OverrunCheckResponseDto checkOverrunLimit(
            @Valid @RequestBody OverrunCheckRequestDto request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );
}
