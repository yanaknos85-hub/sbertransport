package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.IndicatorDateInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.Indicators;

import java.util.UUID;

@Validated
@RequestMapping("indicators")
@Tag(name = "Справочник: Показатели", description = "Контроллер для работы с показателями транспорта")
public interface IndicatorsController {
    
    @Operation(summary = "Показатели транспорта", description = "Внесение показателей по выбранному транспорту")
    @PatchMapping("/{transportId}")
    void updateIndicators(
            @PathVariable("transportId") @Parameter(description = "Идентификатор транспортного средства", required = true) UUID transportId,
            @RequestBody @Valid Indicators indicator,
            @Parameter(hidden = true) Authentication authentication
                                 );
    
    @Operation(summary = "Получение года и месяца внесения показателей", description = "Получение года и месяца для внесения показателей")
    @GetMapping("{transportId}")
    IndicatorDateInfo getIndicatorsDateInfo(
            @PathVariable("transportId") @Parameter(description = "Идентификатор транспортного средства", required = true) UUID transportId,
            @Parameter(hidden = true) Authentication authentication
                                           );
    @GetMapping("history/{transportId}")
    @Operation(summary = "Получение показаний одометра", description = "Получение показаний одометра")
    GetIndicatorValueDto getIndicatorValue(@PathVariable("transportId") @Parameter(description = "Идентификатор транспортного средства", required = true) UUID transportId);
}