package ru.sber.transport.dispatcher.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequestMapping("/{contractorId}/attribute/")
@Tag(name = "Признаки водителей контрагента", description = "Набор операций для работы с признаками водителей")
public interface DriverAttributeController {

    /**
     * Добавить новый признак водителей для контрагента
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового признака водителей")
    List<AttributeDTO> add(@PathVariable("contractorId") UUID contractorId, @RequestBody @Valid List<NewAttributeDTO> tags);

    /**
     * Изменение признака водителец
     */
    @PutMapping(value = "/{tagId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение признака водителей")
    void edit(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("tagId") UUID tagId,
            @RequestBody @Valid NewAttributeDTO tag
    );

    /**
     * Удаление признака водителей
     */
    @DeleteMapping("/{tagId}/")
    @Operation(summary = "Удаление", description = "Удаление признака водителей по id")
    void delete(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("tagId") UUID tagId
    );

    /**
     * Получение признака по id
     */
    @GetMapping(value = "/{tagId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение одного", description = "Получение признака водителей по id")
    AttributeDTO get(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("tagId") UUID tagId
    );

    /**
     * Получение всех признаков водителей данного контрагента
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех признаков водителей данного контрагента")
    Collection<AttributeDTO> getAll(@PathVariable("contractorId") UUID contractorId);

}
