package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DispatcherSearchDto;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с диспетчерами.
 */
@RequestMapping("/")
@Tag(name = "Диспетчеры", description = "Набор операций для работы с диспетчерами контрагента")
public interface DispatcherController {

    @SkipConsentCheck
    @GetMapping(value = "/self/dispatcher/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Личный профиль диспетчера", description = "Личный профиль диспетчера")
    DispatcherDto getSelfProfile(@Parameter(hidden = true) Authentication authentication);

    @SkipConsentCheck
    @Deprecated(forRemoval = true, since = "04.008.000")
    @PatchMapping(value = "/self/dispatcher/consent/")
    @Operation(summary = "Подписание Пдн", description = "Подписание Пдн диспетчером", deprecated = true)
    void signPdn(@Parameter(hidden = true) Authentication authentication);

    @SkipConsentCheck
    @PatchMapping(value = "/self/dispatcher/")
    @Operation(summary = "Изменение", description = "Частичное изменение данных")
    void patchDispatcher(
            @RequestBody List<PatchDataV2> data,
            @Parameter(hidden = true) Authentication authentication
    );

    @PostMapping(value = "/{contractorId}/dispatcher/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового диспетчера")
    DispatcherDto add(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestBody @Valid NewDispatcherDto dispatcher
    );

    @PutMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение", description = "Изменение диспетчера")
    void edit(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор диспетчера")
            @PathVariable("dispatcherId") UUID dispatcherId,
            @RequestBody @Valid NewDispatcherDto dispatcher);

    @DeleteMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/")
    @ResponseBody
    @Operation(summary = "Удаление", description = "Удаление диспетчера")
    void delete(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор диспетчера")
            @PathVariable("dispatcherId") UUID dispatcherId
    );

    @DeleteMapping(value = "/{contractorId}/dispatcher/")
    @ResponseBody
    @Operation(summary = "Удаление", description = "Удаление диспетчерской")
    void delete(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId
    );

    @GetMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение диспетчера")
    DispatcherDto get(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор диспетчера")
            @PathVariable("dispatcherId") UUID dispatcherId
    );

    @GetMapping(value = "/{contractorId}/dispatcher/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение диспетчера")
    Iterable<HasName> getAll(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestParam(name = "projection", defaultValue = "FULL", required = false) Projection projection,
            DispatcherSearchDto searchDto
    );

    @PatchMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение диспетчера")
    void patch(@Parameter(description = "Идентификатор контрагента")
               @PathVariable("contractorId") UUID contractorId,
               @Parameter(description = "Идентификатор диспетчера")
               @PathVariable("dispatcherId") UUID dispatcherId,
               @RequestBody List<PatchDataV2> data);
}
