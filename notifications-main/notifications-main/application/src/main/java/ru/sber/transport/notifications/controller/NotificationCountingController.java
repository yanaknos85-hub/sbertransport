package ru.sber.transport.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.notifications.dto.counting.CountingDto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с количественным триггером отправки уведомлений.
 */
@Validated
@RequestMapping({"/{organizationId}/settings/{id}/counting/", "/{organizationId}/settings/{id}/counting"})
@Tag(name = "Настройки количественного триггера уведомлений",
     description = "Набор операций для работы с настройками количественного триггера отправки уведомлений")
public interface NotificationCountingController {
    
    /**
     * Получение данных количественного триггера уведомления.
     *
     * @param id идентификатор настроек уведомления.
     *
     * @return данные количественного триггера.
     */
    @Operation(summary = "Получить", description = "Получение данных количественного триггера отправки уведомления")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<CountingDto> get(@PathVariable("organizationId") UUID organizationId, @PathVariable("id") UUID id);
    
    /**
     * Установка количественного триггера отправки уведомлений.
     *
     * @param id идентификатор настроек уведомления.
     * @param data новая информация о количественных триггерах.
     */
    @Operation(summary = "Установка количественных триггеров",
               description = "Установка новых количественных триггеров отправки уведомлений")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void set(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") UUID id, @RequestBody @Valid List<@Valid CountingDto> data
            );
    
}
