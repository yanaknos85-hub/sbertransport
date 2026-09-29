package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("model")
@Tag(name = "Работа со справочником Модели ТС", description = "Контроллер для работы с Моделью ТС")
public interface ModelController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Модели ТС по уникальному идентификатору")
    @GetMapping("{modelId}")
    ModelDto getById(
            @PathVariable("modelId") @Parameter(description = "ИД Модели ТС", required = true) @NotNull UUID modelId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Модели ТС")
    @PostMapping
    ModelDto add(
            @RequestBody @Valid @Parameter(description = "Данные Модели ТС", required = true) ModelRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Модели ТС")
    @PutMapping("{modelId}")
    void update(
            @PathVariable("modelId") @Parameter(description = "ИД Модели ТС", required = true) @NotNull UUID modelId,
            @RequestBody @Valid @Parameter(description = "Данные Модели ТС", required = true) ModelRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Модели ТС")
    @DeleteMapping("{modelId}")
    void delete(@PathVariable("modelId") @Parameter(description = "ИД Модели ТС", required = true) @NotNull UUID modelId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Модели ТС")
    @PostMapping("all")
    Page<ModelDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
