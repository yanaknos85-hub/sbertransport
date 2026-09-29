package ru.sber.transport.authsb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authsb.dto.ErrorResponseDto;
import ru.sber.transport.authsb.dto.TokenResponseDto;

@RequestMapping("/auth")
@Validated
@Tag(name = "Адреса", description = "Поиск адресов ВСП")
public interface AuthenticationController {

    @GetMapping("/{sessionId}")
    @ResponseBody
    @Operation(summary = "Получение ссылки на авторизацию"
            , description = "Возвращает URL для перехода к авторизации"
            , responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ссылка сформирована",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Сессия с таким идентификатором уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
            })
    ResponseEntity<?> createUrl(@PathVariable("sessionId") @NotNull String sessionId);

    @PostMapping(value = "/login/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обмен authorization code на токены доступа"
            , description = "Получить долгосрочные токены доступа (access_token, refresh_token, id_token) в обмен на одноразовый authorization_code"
            , responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "токен авторизации плучен",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Нет авторизации",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    ResponseEntity<?> getAuthLogin(
            @Parameter(name = "code") String code,
            @Parameter(name = "state") String state);

    @PostMapping(value = "/refresh/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обновление токенов"
            , description = "Обновление рефреш токена")
    ResponseEntity<?> refresh(
            @RequestParam(name = "refresh_token") String token);
}
