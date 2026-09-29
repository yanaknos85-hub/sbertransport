package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.tariff.dto.NewPersonalTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PersonalTariffDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер тарифов на компенсацию личного транспорта
 */
@RequestMapping({"personal","personal/"})
@Tag(name = "Тарифы личного транспорта", description = "Контроллер для работы с тарифами на компенсацию линччого " +
                                                       "транспорта")
public interface PersonalTariffController {
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
    @Operation(summary = "Добавление", description = "Добавление нового тарифа личного авто")
    PersonalTariffDTO add(@RequestBody @Valid NewPersonalTariffDTO newData);
    
    /**
     * Редактирование существующего тарифа такси
     *
     * @param tariffId ID тарифа.
     * @param newData новые данные тарифа
     */
    @PutMapping(value = {"{tariffId}","{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных тарифа личного авто")
    void edit(
            @PathVariable("tariffId") UUID tariffId,
            @RequestBody @Valid NewPersonalTariffDTO newData
             );
    
    /**
     * Удалить существующий тариф такси
     *
     * @param tariffId ID тарифа
     */
    @DeleteMapping(value = {"{tariffId}","{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа личного авто")
    void delete(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получить существующий тариф личного авто
     *
     * @param tariffId ID тарифа для получения
     *
     * @return данные тарифа.
     */
    @GetMapping(value = {"{tariffId}","{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных тарифа личного авто")
    PersonalTariffDTO get(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение данных всех тарифов личного авто
     *
     * @param regionId Идентификатор региона для фильтрации.
     * @return коллекция тарифов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов личного авто")
    List<? extends PersonalTariffDTO> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId
                                            );
}
