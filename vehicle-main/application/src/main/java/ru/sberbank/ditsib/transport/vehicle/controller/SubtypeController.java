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
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("subtype")
@Tag(name = "Работа со справочником Подвида ТС", description = "Контроллер для работы с Подвидом ТС")
public interface SubtypeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Подвида ТС по уникальному идентификатору")
    @GetMapping("{subtypeId}")
    SubtypeDto getById(
            @PathVariable("subtypeId") @Parameter(description = "ИД Подвида ТС", required = true) @NotNull UUID subtypeId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Подвида ТС")
    @PostMapping
    SubtypeDto add(
            @RequestBody @Valid @Parameter(description = "Данные Подвида ТС", required = true) SubtypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Подвида ТС")
    @PutMapping("{subtypeId}")
    void update(
            @PathVariable("subtypeId") @Parameter(description = "ИД Подвида ТС", required = true) @NotNull UUID subtypeId,
            @RequestBody @Valid @Parameter(description = "Данные Подвида ТС", required = true) SubtypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Подвида ТС")
    @DeleteMapping("{subtypeId}")
    void delete(@PathVariable("subtypeId") @Parameter(description = "ИД Подвида ТС", required = true) @NotNull UUID subtypeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Подвидов ТС")
    @PostMapping("all")
    Page<SubtypeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
