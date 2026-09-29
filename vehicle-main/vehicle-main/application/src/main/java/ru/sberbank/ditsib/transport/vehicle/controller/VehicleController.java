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
import ru.sberbank.ditsib.transport.vehicle.dto.PageVehicleWithFilters;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("vehicle")
@Tag(name = "Работа со справочником Автомобили", description = "Контроллер для работы с справочником Автомобили")
public interface VehicleController {
    @Operation(summary = "Добавление", description = "Добавление Вида ТС")
    @PostMapping
    void add(
            @RequestBody @Valid @Parameter(description = "Данные Автомобиля", required = true) VehicleRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление записи Автомобиля")
    @DeleteMapping("{vehicleId}")
    void delete(@PathVariable("vehicleId") @Parameter(description = "ИД Автомобиля", required = true) @NotNull UUID vehicleId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Автомобилей")
    @PostMapping("all")
    Page<VehicleShortDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
    
    @Operation(summary = "Поиск автомобилей по фильтрам", description = "Поиск автомобилей по фильтрам")
    @PostMapping("search")
    PageVehicleWithFilters search(@RequestBody(required = false) VehicleSearchDto searchDto);
}
