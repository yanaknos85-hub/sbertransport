package ru.sber.transport.trip.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с настройками отображаемых столбцов.
 */
@RequestMapping("/settings/reports/columns/")
@Tag(name = "Настройки", description = "Набор операций для работы с настройками отображаемых столбцов")
public interface ColumnSettingsController {

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание", description = "Создание настроек отображаемых столбцов")
    Map<String, Object> createColumnSetting(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestBody Map<String, Object> setting
    );

    @PutMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение настроек отображаемых столбцов")
    void updateColumnSetting(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestBody Map<String, Object> setting
    );

    @DeleteMapping(value = "/")
    @Operation(summary = "Удаление", description = "Удаление настроек отображаемых столбцов")
    void deleteColumnSetting(
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение настроек отображаемых столбцов")
    Map<String, Object> getColumnSetting(
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

}
