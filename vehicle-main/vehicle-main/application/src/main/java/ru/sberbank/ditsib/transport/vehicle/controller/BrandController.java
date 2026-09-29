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
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("brand")
@Tag(name = "Справочник: Марка ТС", description = "Контроллер для работы с маркой ")
public interface BrandController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Марки ТС по уникальному идентификатору")
    @GetMapping("{brandId}")
    BrandDto getById(
            @PathVariable("brandId") @Parameter(description = "ИД Марки ТС", required = true) @NotNull UUID brandId,
            @Parameter(hidden = true) Authentication authentication
                    );
    
    @Operation(summary = "Добавление", description = "Добавление Марки ТС")
    @PostMapping
    BrandDto add(
            @RequestBody @Valid @Parameter(description = "Данные Марки ТС", required = true) BrandRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
                );
    
    @Operation(summary = "Изменение", description = "Обновление Марки ТС")
    @PutMapping("{brandId}")
    void update(
            @PathVariable("brandId") @Parameter(description = "ИД Марки ТС", required = true) @NotNull UUID brandId,
            @RequestBody @Valid @Parameter(description = "Данные Марки ТС", required = true) BrandRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Марки ТС")
    @DeleteMapping("{brandId}")
    void delete(@PathVariable("brandId") @Parameter(description = "ИД Марки ТС", required = true) @NotNull UUID brandId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Марок ТС")
    @PostMapping("all")
    Page<BrandDto> getAll(@RequestBody PaginationCommonRequestDto paginationRequest);
}
