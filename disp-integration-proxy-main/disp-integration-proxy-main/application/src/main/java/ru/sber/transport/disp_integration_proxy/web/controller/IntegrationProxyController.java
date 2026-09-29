package ru.sber.transport.disp_integration_proxy.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import java.util.Map;

/**
 * Контроллер проксирования запросов
 */
@RequestMapping("/")
@Tag(name = "Прокси", description = "Набор операций для проксирования запросов в сервисы исполнения поездок")
public interface IntegrationProxyController {

    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Авторизация", description = "Авторизация в диспетчерской")
    @NoAuthorize
    Object auth();

    @PostMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание", description = "Создание поездки в диспетчерской")
    @NoAuthorize
    Object createTrip(
            @RequestHeader("x-transportation-type") String transportationType,
            @RequestBody Object body
    );

    @GetMapping(value = "/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение поездки диспетчерской")
    @NoAuthorize
    Object getTrip(
            @RequestHeader("x-transportation-type") String transportationType,
            @PathVariable("orderId") String orderId
    );

    @PostMapping(value = "/orders/{orderId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание", description = "Создание поездки в диспетчерской")
    @NoAuthorize
    Object cancelTrip(
            @RequestHeader("x-transportation-type") String transportationType,
            @PathVariable("orderId") String orderId
    );
}
