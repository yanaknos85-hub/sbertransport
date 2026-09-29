package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.tariff.dto.CarSharingTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewCarSharingTariffDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер тарифов каршеринга
 */
@RequestMapping({"carsharing","carsharing/"})
@Tag(name = "Тарифы каршеринга", description = "Контроллер для работы с тарифами каршеринга")
public interface CarSharingTariffController {
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
    @Operation(summary = "Добавление", description = "Добавление нового тарифа каршеринга")
    CarSharingTariffDTO add(@RequestBody @Valid NewCarSharingTariffDTO newData,
                            @Parameter(hidden = true) JwtAuthenticationToken authentication
                           );
    
    /**
     * Редактирование существующего тарифа каршеринга
     *
     * @param tariffId ID тарифа.
     * @param newData новые данные тарифа
     */
    @PutMapping(value = {"{tariffId}","{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных тарифа каршеринга")
    void edit(
            @PathVariable("tariffId") UUID tariffId,
            @RequestBody @Valid NewCarSharingTariffDTO newData
             );
    
    /**
     * Удалить существующий тариф каршеринга
     *
     * @param tariffId ID тарифа
     */
    @DeleteMapping(value = {"{tariffId}","{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа каршеринга")
    void delete(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получить существующий тариф каршеринга
     *
     * @param tariffId ID тарифа для получения
     *
     * @return данные тарифа.
     */
    @GetMapping(value = {"{tariffId}","{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных тарифа каршеринга")
    CarSharingTariffDTO get(@PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение данных всех тарифов каршеринга
     *
     * @param regionId Идентификатор региона для фильтрации.
     * @return коллекция тарифов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов каршеринга")
    List<? extends CarSharingTariffDTO> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId
                                              );
}
