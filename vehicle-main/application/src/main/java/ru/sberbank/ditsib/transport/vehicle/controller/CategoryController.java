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
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("category")
@Tag(name = "Работа со справочником Категорий ТС", description = "Контроллер для работы с Категорий ТС")
public interface CategoryController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Категории ТС по уникальному идентификатору")
    @GetMapping("{categoryId}")
    CategoryDto getById(
            @PathVariable("categoryId") @Parameter(description = "ИД Категории ТС", required = true) @NotNull UUID categoryId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Категории ТС")
    @PostMapping
    CategoryDto add(
            @RequestBody @Valid @Parameter(description = "Данные Категории ТС", required = true) CategoryRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Категории ТС")
    @PutMapping("{categoryId}")
    void update(
            @PathVariable("categoryId") @Parameter(description = "ИД Категории ТС", required = true) @NotNull UUID categoryId,
            @RequestBody @Valid @Parameter(description = "Данные Категории ТС", required = true) CategoryRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Категории ТС")
    @DeleteMapping("{categoryId}")
    void delete(@PathVariable("categoryId") @Parameter(description = "ИД Категории ТС", required = true) @NotNull UUID categoryId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Категории ТС")
    @PostMapping("all")
    Page<CategoryDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
