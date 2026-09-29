package ru.sber.transport.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Контроллер для работы с ограничениями отправки уведомлений.
 */
@RequestMapping({"/{organizationId}/settings/{id}/restrict/", "/{organizationId}/settings/{id}/restrict"})
@Validated
@Tag(name = "Настройки ограничений",
     description = "Набор операций для работы с настройками ограничений отправки уведомлений")
public interface NotificationRestrictionsController {
    
    /**
     * Получение списка ограничений на уведомление.
     *
     * @param id идентификатор настроек уведомления.
     *
     * @return информация об ограничениях.
     */
    @Operation(summary = "Список ограничений", description = "Получение списка ограничений на уведомление")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    RestrictionDataDto get(@PathVariable("organizationId") UUID organizationId, @PathVariable("id") UUID id);
    
    /**
     * Установка ограничения на отправку уведомлений.
     *
     * @param id идентификатор настроек уведомления.
     * @param data новая информация об ограничениях.
     */
    @Operation(summary = "Установка ограничений", description = "Установка ограничения на уведомления")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void set(
            @PathVariable("organizationId") UUID organizationId, @PathVariable("id") UUID id,
            @RequestBody @Valid RestrictionDataDto data
            );
    
}
