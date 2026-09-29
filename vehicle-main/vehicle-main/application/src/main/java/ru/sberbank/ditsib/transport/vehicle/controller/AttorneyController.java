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
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyPaginationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyUpdateDto;

import java.util.UUID;

@Validated
@RequestMapping("attorney")
@Tag(name = "Справочник: МЧД", description = "Контроллер для работы с МЧД")
public interface AttorneyController {

    @Operation(summary = "Получение списком", description = "Получение списка МЧД")
    @PostMapping("all")
    Page<AttorneyDto> getAll(@RequestBody AttorneyPaginationRequestDto paginationRequest,
                             @Parameter(hidden = true) Authentication authentication);


    @Operation(summary = "Добавление", description = "Добавление МЧД")
    @PostMapping
    AttorneyDto add(@RequestBody @Valid @Parameter(description = "Данные МЧД", required = true) AttorneyCreateDto requestDto,
                    @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Изменение", description = "Изменение МЧД")
    @PatchMapping("{attorneyId}")
    AttorneyDto update(@RequestBody @Valid @Parameter(description = "Данные МЧД", required = true) AttorneyUpdateDto requestDto,
                    @PathVariable("attorneyId") @NotNull @Valid UUID  attorneyId,
                    @Parameter(hidden = true) Authentication authentication);

    @GetMapping
    @Operation(summary = "Получение МЧД", description = "Получение МЧД по ID телемеханика")
    AttorneyDto getAttorneyByTelemechanicId(@Parameter(hidden = true) Authentication authentication);
}
