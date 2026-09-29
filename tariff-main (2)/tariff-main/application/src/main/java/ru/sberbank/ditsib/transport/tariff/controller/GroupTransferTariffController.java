package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.tariff.dto.GroupTransferTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewGroupTransferTariffDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер тарифов группового трансфера
 */
@RequestMapping({"group_transfer","group_transfer/"})
@Tag(name = "Тарифы группового трансфера", description = "Контроллер для работы с тарифами группового трансфера")
public interface GroupTransferTariffController {
    
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
    @Operation(summary = "Добавление", description = "Добавление нового тарифа группового трансфера")
    GroupTransferTariffDTO add(@RequestBody @Valid NewGroupTransferTariffDTO newData);
    
    /**
     * Редактирование существующего тарифа группового трансфера
     *
     * @param tariffId ID тарифа.
     * @param newData новые данные тарифа
     */
    @PutMapping(value = {"{tariffId}","{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных тарифа группового трансфера")
    void edit(
            @PathVariable("tariffId") UUID tariffId,
            @RequestBody @Valid NewGroupTransferTariffDTO newData
             );
    
    /**
     * Удалить существующий тариф группового трансфера
     *
     * @param tariffId ID тарифа
     */
    @DeleteMapping(value = {"{tariffId}","{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа группового трансфера")
    void delete(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получить существующий тариф группового трансфера
     *
     * @param tariffId ID тарифа для получения
     *
     * @return данные тарифа.
     */
    @GetMapping(value = {"{tariffId}","{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных тарифа группового трансфера")
    GroupTransferTariffDTO get(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение данных всех тарифов группового трансфера
     *
     * @param regionId Идентификатор региона для фильтрации.
     *
     * @return коллекция тарифов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов группового трансфера")
    List<? extends GroupTransferTariffDTO> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId
                                                 );
}
