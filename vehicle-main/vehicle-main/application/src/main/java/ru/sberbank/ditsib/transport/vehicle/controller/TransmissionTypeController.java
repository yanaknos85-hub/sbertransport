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
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Validated
@RequestMapping("transmission-type")
@Tag(name = "Работа со справочником Типа трансмиссий ТС", description = "Контроллер для работы с Типами трансмиссий ТС")
public interface TransmissionTypeController {
    
    @Operation(summary = "Получение по ИД", description = "Получение трансмиссии ТС по уникальному идентификатору")
    @GetMapping("{transmissionTypeId}")
    TransmissionTypeDto getById(
            @PathVariable("transmissionTypeId") @Parameter(description = "ИД трансмиссии ТС", required = true) @NotNull UUID transmissionTypeId,
            @Parameter(hidden = true) Authentication authentication
                   );
    
    @Operation(summary = "Добавление", description = "Добавление трансмиссии ТС")
    @PostMapping
    TransmissionTypeDto add(
            @RequestBody @Valid @Parameter(description = "Данные трансмиссии ТС", required = true) TransmissionTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Изменение", description = "Обновление трансмиссии ТС")
    @PutMapping("{transmissionTypeId}")
    void update(
            @PathVariable("transmissionTypeId") @Parameter(description = "ИД трансмиссии ТС", required = true) @NotNull UUID transmissionTypeId,
            @RequestBody @Valid @Parameter(description = "Данные трансмиссии ТС", required = true) TransmissionTypeRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication
               );
    
    @Operation(summary = "Удаление", description = "Удаление трансмиссии ТС")
    @DeleteMapping("{transmissionTypeId}")
    void delete(@PathVariable("transmissionTypeId") @Parameter(description = "ИД трансмиссии ТС", required = true) @NotNull UUID transmissionTypeId);
    
    @Operation(summary = "Получение списком", description = "Получение списка трансмиссии ТС")
    @PostMapping("all")
    Page<TransmissionTypeDto> getAll(@RequestBody(required = false) PaginationCommonRequestDto paginationRequest);
}
