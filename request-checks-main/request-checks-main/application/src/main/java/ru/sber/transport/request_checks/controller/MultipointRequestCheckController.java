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
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;

@Tag(
    name = "API для проверки многоточечных поездок",
    description = "Проверка лимита многоточечных поездок (с количеством адресов > 2)"
)
@Validated
@RequestMapping("/multipoint")
public interface MultipointRequestCheckController {

    @Operation(summary = "Проверка лимита многоточечных поездок",
        description = "Проверка лимита многоточечных поездок (с количеством адресов > 2) на одну дату.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для проверки многоточечной поездки",
            required = true,
            content = @Content(schema = @Schema(implementation = MultipointCheckRequestDto.class))
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
                description = "Превышен лимит поездок с количеством точек > 2"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера"
            )
        }
    )
    @PostMapping("/limit")
    void checkMultipointLimit(
        @Valid @RequestBody MultipointCheckRequestDto request,
        @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

}
