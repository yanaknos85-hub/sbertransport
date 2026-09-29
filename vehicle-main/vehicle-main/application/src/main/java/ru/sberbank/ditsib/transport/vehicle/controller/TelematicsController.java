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
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("telematics")
@Tag(name = "Справочник: Телематика", description = "Контроллер для работы с телематикой")
public interface TelematicsController {

    @Operation(summary = "Получение по ИД", description = "Получение Телематики по уникальному идентификатору")
    @GetMapping("{telematicsId}")
    TelematicsDto getById(
            @PathVariable("telematicsId") @Parameter(description = "ИД Телематики", required = true) @NotNull UUID telematicsId,
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "Добавление", description = "Добавление Телематики")
    @PostMapping
    TelematicsDto add(
            @RequestBody @Valid @Parameter(description = "Данные Телематики", required = true) TelematicsRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "Изменение", description = "Обновление Телематики")
    @PutMapping("{telematicsId}")
    void update(
            @PathVariable("telematicsId") @Parameter(description = "ИД Телематики", required = true) @NotNull UUID telematicsId,
            @RequestBody @Valid @Parameter(description = "Данные Телематики", required = true) TelematicsRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "Удаление", description = "Удаление Телематики")
    @DeleteMapping("{telematicsId}")
    void delete(@PathVariable("telematicsId") @Parameter(description = "ИД Телематики", required = true) @NotNull UUID telematicsId);

    @Operation(summary = "Получение списком", description = "Получение списка списка Телематик")
    @PostMapping("all")
    Page<TelematicsDto> getAll(@RequestBody PaginationCommonRequestDto paginationRequest);
}
