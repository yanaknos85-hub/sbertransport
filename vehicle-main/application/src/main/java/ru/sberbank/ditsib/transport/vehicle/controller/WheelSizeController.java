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
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("wheel-size")
@Tag(name = "Работа со справочником Размер колеса ТС", description = "Контроллер для работы с Размером колеса ТС")
public interface WheelSizeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Размера колеса ТС по уникальному идентификатору")
    @GetMapping("{wheelSizeId}")
    WheelSizeDto getById(
            @PathVariable("wheelSizeId") @Parameter(description = "ИД Размера колеса ТС", required = true) @NotNull UUID wheelSizeId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Размера колеса ТС")
    @PostMapping
    WheelSizeDto add(
            @RequestBody @Valid @Parameter(description = "Данные Размера колеса ТС", required = true) WheelSizeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Размера колеса ТС")
    @PutMapping("{wheelSizeId}")
    void update(
            @PathVariable("wheelSizeId") @Parameter(description = "ИД Размера колеса ТС", required = true) @NotNull UUID wheelSizeId,
            @RequestBody @Valid @Parameter(description = "Данные Размера колеса ТС", required = true) WheelSizeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Размера колеса ТС")
    @DeleteMapping("{wheelSizeId}")
    void delete(@PathVariable("wheelSizeId") @Parameter(description = "ИД Размера колеса ТС", required = true) @NotNull UUID wheelSizeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Размера колеса ТС")
    @PostMapping("all")
    Page<WheelSizeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
