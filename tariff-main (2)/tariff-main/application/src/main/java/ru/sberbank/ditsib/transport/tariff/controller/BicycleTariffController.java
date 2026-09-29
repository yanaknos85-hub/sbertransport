package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.tariff.dto.BicycleTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewBicycleTariffDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер тарифов велосипеда
 */
@RequestMapping({"bicycle","bicycle/"})
@Tag(name = "Тарифы велосипеда", description = "Контроллер для работы с тарифами велосипеда")
public interface BicycleTariffController {
    /**
     * Добавить новый тариф
     *
     * @param newData данные нового тарифа
     *
     * @return Данные созданного тарифа
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового тарифа велосипеда")
    BicycleTariffDTO add(@RequestBody @Valid NewBicycleTariffDTO newData);
    
    /**
     * Редактирование существующего тарифа велосипеда
     *
     * @param tariffId ID тарифа.
     * @param newData новые данные тарифа
     */
    @PutMapping(value = {"{tariffId}","{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных тарифа велосипеда")
    void edit(
            @PathVariable("tariffId") UUID tariffId,
            @RequestBody @Valid NewBicycleTariffDTO newData
             );
    
    /**
     * Удалить существующий тариф велосипеда
     *
     * @param tariffId ID тарифа
     */
    @DeleteMapping(value = {"{tariffId}","{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа велосипеда")
    void delete(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получить существующий тариф велосипеда
     *
     * @param tariffId ID тарифа для получения
     *
     * @return данные тарифа.
     */
    @GetMapping(value = {"{tariffId}","{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных тарифа велосипеда")
    BicycleTariffDTO get(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение данных всех тарифов велосипеда
     *
     * @param regionId Идентификатор региона для фильтрации.
     * @return коллекция тарифов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов велосипеда")
    List<? extends BicycleTariffDTO> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId
                                           );
}
