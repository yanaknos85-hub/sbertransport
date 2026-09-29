package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.tariff.dto.NewPublicTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PublicTariffDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер тарифов общественного транспорта
 */
@RequestMapping({"public","public/"})
@Tag(name = "Тарифы общественного транспорта", description ="Контроллер для работы с тарифами общественного транспорта")
public interface PublicTariffController {
    /**
     * Добавить новый тариф общественного транспорта
     *
     * @param newData данные нового тарифа
     *
     * @return Данные созданного тарифа
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового тарифа общественного транспорта")
    PublicTariffDTO add(@RequestBody @Valid NewPublicTariffDTO newData);
    
    /**
     * Редактирование существующего тарифа общественного транспорта
     *
     * @param tariffId ID тарифа
     * @param newData новые данные тарифа
     */
    @PutMapping(value = {"{tariffId}","{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных тарифа общественного транспорта")
    void edit(
            @PathVariable("tariffId") UUID tariffId,
            @RequestBody @Valid NewPublicTariffDTO newData
    );
    
    /**
     * Удалить существующий тариф общественного транспорта
     *
     * @param tariffId ID тарифа
     */
    @DeleteMapping(value = {"{tariffId}","{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа общественного транспорта")
    void delete(@PathVariable("tariffId") UUID tariffId);

    /**
     * Получить существующий тариф общественного транспорта
     *
     * @param tariffId ID тарифа для получения
     *
     * @return данные тарифа
     */
    @GetMapping(value = {"{tariffId}","{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных тарифа общественного транспорта")
    PublicTariffDTO get(@PathVariable("tariffId") UUID tariffId);

    /**
     * Получение данных всех тарифов общественного транспорта
     *
     * @param regionId Идентификатор региона для фильтрации.
     * @return коллекция тарифов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов общественного транспорта")
    List<? extends PublicTariffDTO> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId
                                          );
}
