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
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("engine-type")
@Tag(name = "Справочник: Тип двигателя ТС", description = "Контроллер для работы с типом двигателя ТС ")
public interface EngineTypeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Типа двигателя ТС по уникальному идентификатору")
    @GetMapping("{typeId}")
    EngineTypeDto getById(
            @PathVariable("typeId") @Parameter(description = "ИД Типа двигателя ТС", required = true) @NotNull UUID typeId,
            @Parameter(hidden = true) Authentication authentication
                    );
    
    @Operation(summary = "Добавление", description = "Добавление Типа двигателя ТС")
    @PostMapping
    EngineTypeDto add(
            @RequestBody @Valid @Parameter(description = "Данные Типа двигателя ТС", required = true) EngineTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
                );
    
    @Operation(summary = "Изменение", description = "Обновление Типа двигателя ТС")
    @PutMapping("{typeId}")
    void update(
            @PathVariable("typeId") @Parameter(description = "ИД Типа двигателя ТС", required = true) @NotNull UUID typeId,
            @RequestBody @Valid @Parameter(description = "Данные Типа двигателя ТС", required = true) EngineTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Типа двигателя ТС")
    @DeleteMapping("{typeId}")
    void delete(@PathVariable("typeId") @Parameter(description = "ИД Типа двигателя ТС", required = true) @NotNull UUID typeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Типов двигателей ТС")
    @PostMapping("all")
    Page<EngineTypeDto> getAll(@RequestBody PaginationCommonRequestDto paginationRequest);
}
