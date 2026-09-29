package ru.sber.transport.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.notifications.dto.timing.TimingDto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с таймингом отправки уведомлений.
 */
@Validated
@RequestMapping({"/{organizationId}/settings/{id}/timing/", "/{organizationId}/settings/{id}/timing"})
@Tag(name = "Настройки тайминга уведомлений",
     description = "Набор операций для работы с настройками тайминга отправки уведомлений")
public interface NotificationTimingController {
    
    /**
     * Получение данных тайминга уведомления.
     *
     * @param id идентификатор настроек уведомления.
     *
     * @return данные тайминга.
     */
    @Operation(summary = "Получить", description = "Получение данных тайминга отправки уведомления")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<TimingDto> get(@PathVariable("organizationId") UUID organizationId, @PathVariable("id") UUID id);
    
    /**
     * Установка тайминга отправки уведомлений.
     *
     * @param id идентификатор настроек уведомления.
     * @param data новая информация о тайминге.
     */
    @Operation(summary = "Установка тайминга", description = "Установка новых данных тайминга отправки уведомлений")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void set(
            @PathVariable("organizationId") UUID organizationId, @PathVariable("id") UUID id,
            @RequestBody @Valid List<@Valid TimingDto> data
            );
    
}
