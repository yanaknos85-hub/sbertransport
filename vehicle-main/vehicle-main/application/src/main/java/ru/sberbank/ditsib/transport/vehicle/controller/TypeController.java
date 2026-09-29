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
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("type")
@Tag(name = "Работа со справочником Видов ТС", description = "Контроллер для работы с Видом ТС")
public interface TypeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Вида ТС по уникальному идентификатору")
    @GetMapping("{typeId}")
    TypeDto getById(
            @PathVariable("typeId") @Parameter(description = "ИД Вида ТС", required = true) @NotNull UUID typeId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Вида ТС")
    @PostMapping
    TypeDto add(
            @RequestBody @Valid @Parameter(description = "Данные вида ТС", required = true) TypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Вида ТС")
    @PutMapping("{typeId}")
    void update(
            @PathVariable("typeId") @Parameter(description = "ИД Вида ТС", required = true) @NotNull UUID typeId,
            @RequestBody @Valid @Parameter(description = "Данные вида ТС", required = true) TypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Вида ТС")
    @DeleteMapping("{typeId}")
    void delete(@PathVariable("typeId") @Parameter(description = "ИД Марки ТС", required = true) @NotNull UUID typeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Видов ТС")
    @PostMapping("all")
    Page<TypeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
