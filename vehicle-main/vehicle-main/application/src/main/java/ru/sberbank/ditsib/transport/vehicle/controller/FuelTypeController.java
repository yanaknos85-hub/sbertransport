package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("fuel-type")
@Tag(name = "Работа со справочником Вида топлива ТС", description = "Контроллер для работы с Видом топлива ТС")
public interface FuelTypeController {

    @Operation(summary = "Получение по ИД", description = "Получение Вида топлива ТС по уникальному идентификатору")
    @GetMapping("{fuelTypeId}")
    FuelTypeDto getById(
            @PathVariable("fuelTypeId") @Parameter(description = "ИД Вида топлива ТС", required = true) @NotNull UUID fuelTypeId,
            @Parameter(hidden = true) Authentication authentication
    );

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавление", description = "Добавление Вида топлива ТС")
    @PostMapping
    void add(
            @RequestBody @Valid @Parameter(description = "Данные Вида топлива ТС", required = true) FuelTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "Изменение", description = "Обновление Вида топлива ТС")
    @PutMapping("{fuelTypeId}")
    void update(
            @PathVariable("fuelTypeId") @Parameter(description = "ИД Вида топлива ТС", required = true) @NotNull UUID fuelTypeId,
            @RequestBody @Valid @Parameter(description = "Данные Вида топлива ТС", required = true) FuelTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "Удаление", description = "Удаление Вида топлива ТС")
    @DeleteMapping("{fuelTypeId}")
    void delete(@PathVariable("fuelTypeId") @Parameter(description = "ИД Вида топлива ТС", required = true) @NotNull UUID fuelTypeId);

    @Operation(summary = "Получение списком", description = "Получение списка Вида топлива ТС")
    @PostMapping("all")
    Page<FuelTypeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
