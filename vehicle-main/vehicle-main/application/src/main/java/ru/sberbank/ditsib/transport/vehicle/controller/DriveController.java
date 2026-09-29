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
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("drive")
@Tag(name = "Справочник: Привод ТС", description = "Контроллер для работы с приводом ТС ")
public interface DriveController {
    
    @Operation(summary = "Получение по ИД", description = "Получение привода ТС по уникальному идентификатору")
    @GetMapping("{driveId}")
    DriveDto getById(
            @PathVariable("driveId") @Parameter(description = "ИД Привода ТС", required = true) @NotNull UUID driveId,
            @Parameter(hidden = true) Authentication authentication
                    );
    
    @Operation(summary = "Добавление", description = "Добавление Привода ТС")
    @PostMapping
    DriveDto add(
            @RequestBody @Valid @Parameter(description = "Данные Привода ТС", required = true) DriveRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
                );
    
    @Operation(summary = "Изменение", description = "Обновление Привода ТС")
    @PutMapping("{driveId}")
    void update(
            @PathVariable("driveId") @Parameter(description = "ИД Привода ТС", required = true) @NotNull UUID driveId,
            @RequestBody @Valid @Parameter(description = "Данные Привода ТС", required = true) DriveRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Привода ТС")
    @DeleteMapping("{driveId}")
    void delete(@PathVariable("driveId") @Parameter(description = "ИД Привода ТС", required = true) @NotNull UUID driveId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Приводов ТС")
    @PostMapping("all")
    Page<DriveDto> getAll(@RequestBody PaginationCommonRequestDto paginationRequest);
}
