package ru.sber.transport.etrn.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.etrn.dto.*;

import java.util.UUID;

@Tag(name = "ЭТрН", description = "Управление электронными транспортными накладными")
public interface EtrnController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создание ЭТрН", description = "Идемпотентное создание по humanReadableId")
    ResponseEntity<EtrnDto> create(@RequestBody @Valid EtrnCreateRequest request);

    @PostMapping("/list")
    @Operation(summary = "Журнал ЭТрН", description = "Список с пагинацией и сортировкой")
    ResponseEntity<Page<EtrnJournalDto>> list(@RequestBody @Valid SearchEtrnDto request);

    @GetMapping("/{id}")
    @Operation(summary = "Детальная информация")
    ResponseEntity<EtrnDetailDto> getById(@PathVariable UUID id);

    @PutMapping("/{id}/lock")
    @Operation(summary = "Установка блокировки")
    ResponseEntity<Void> lock(@PathVariable UUID id, @Parameter(hidden = true) Authentication authentication);

    @DeleteMapping("/{id}/lock")
    @Operation(summary = "Снятие блокировки")
    ResponseEntity<Void> unlock(@PathVariable UUID id, @Parameter(hidden = true) Authentication authentication);

    @GetMapping("/attorneyCheck")
    @Operation(
            summary = "Проверка доверенностей",
            description = "Проверка доверенностей текущего сотрудника через Dispatcher-сервис.\n"
                    + "Возвращает информацию о действующей доверенности или ошибку при отсутствии/просрочке."
    )
    ResponseEntity<AttorneyCheckResponseDto> attorneyCheck(
            @Parameter(hidden = true) Authentication authentication
    );
}