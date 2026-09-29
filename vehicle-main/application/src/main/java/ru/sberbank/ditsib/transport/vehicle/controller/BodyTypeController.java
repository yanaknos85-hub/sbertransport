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
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("body-type")
@Tag(name = "Работа со справочником Типов кузова ТС", description = "Контроллер для работы с Типов кузова ТС")
public interface BodyTypeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение Типа кузова ТС по уникальному идентификатору")
    @GetMapping("{bodyTypeId}")
    BodyTypeDto getById(
            @PathVariable("bodyTypeId") @Parameter(description = "ИД Типа кузова ТС", required = true) @NotNull UUID bodyTypeId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление Типа кузова ТС")
    @PostMapping
    BodyTypeDto add(
            @RequestBody @Valid @Parameter(description = "Данные Типа кузова ТС", required = true) BodyTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление Типа кузова ТС")
    @PutMapping("{bodyTypeId}")
    void update(
            @PathVariable("bodyTypeId") @Parameter(description = "ИД Типа кузова ТС", required = true) @NotNull UUID bodyTypeId,
            @RequestBody @Valid @Parameter(description = "Данные Типа кузова ТС", required = true) BodyTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление Типа кузова ТС")
    @DeleteMapping("{bodyTypeId}")
    void delete(@PathVariable("bodyTypeId") @Parameter(description = "ИД Типа Кузова ТС", required = true) @NotNull UUID bodyTypeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка Типа кузова ТС")
    @PostMapping("all")
    Page<BodyTypeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
