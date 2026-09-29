package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;

import jakarta.validation.Valid;
import ru.sber.transport.dispatcher.dto.search.AutoparkSearchDTO;

import java.util.Collection;
import java.util.UUID;

@RequestMapping("/{contractorId}/autopark/")
@Tag(name = "Автопарки", description = "Набор операций для работы со справочником автопарков")
public interface AutoparkController {

    /**
     * Add a new transport.
     *
     * @param autopark new data of autopark.
     * @return added transport.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового автопарка контрагента")
    AutoparkDTO add(@PathVariable("contractorId") UUID contractorId, @RequestBody @Valid NewAutoparkDTO autopark);

    /**
     * Edit data about autopark.
     *
     * @param contractorId ID of contractor.
     * @param autoparkId   ID of autopark.
     * @param autopark     new data of autopark.
     */
    @PutMapping(value = "{autoparkId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных автопарка контрагента")
    void edit(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            @RequestBody @Valid NewAutoparkDTO autopark
    );

    /**
     * Delete autopark with ID.
     *
     * @param contractorId ID of contractor to delete.
     * @param autoparkId   ID of driver to delete.
     */
    @DeleteMapping("{autoparkId}/")
    @Operation(summary = "Удаление", description = "Удаление автопарка")
    void delete(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId
    );

    /**
     * Get autopark by ID.
     *
     * @param contractorId ID of contractor to get data.
     * @param autoparkId   ID of transport.
     * @return autopark.
     */
    @GetMapping(value = "{autoparkId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных автопарка контрагента по ID")
    AutoparkDTO get(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId
    );

    /**
     * Get all autoparks.
     *
     * @param contractorId ID of contractor to get data.
     * @param autoparkSearchDTO autopark filters.
     * @return collection with autoparks.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех автопарков контрагента")
    Page<AutoparkDTO> getAll(@PathVariable("contractorId") UUID contractorId,
                             AutoparkSearchDTO autoparkSearchDTO);

}
