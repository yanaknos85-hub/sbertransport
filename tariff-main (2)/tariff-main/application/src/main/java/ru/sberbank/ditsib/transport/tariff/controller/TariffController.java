package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.tariff.model.BaseTariffDataDto;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.dto.ShortTariffDto;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.files.WorkGroupFileDto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с тарифами.
 */
@RequestMapping
@Tag(name = "Тарифы", description = "Контроллер для работы с тарифами")
public interface TariffController {
    
    /**
     * Удаление тарифа.
     *
     * @param tariffId идентификатор тарифа.
     */
    @DeleteMapping(value = {"{transportTypeId}/{tariffId}","{transportTypeId}/{tariffId}/"})
    @Operation(summary = "Удаление", description = "Удаление данных тарифа")
    void delete(@PathVariable("transportTypeId") UUID transportTypeId, @PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение тарифа.
     *
     * @param tariffId идентификатор тарифа.
     *
     * @return данные тарифа.
     */
    @GetMapping(value = {"{transportTypeId}/{tariffId}","{transportTypeId}/{tariffId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @NoAuthorize
    @Operation(summary = "Получение", description = "Получение данных тарифа")
    BaseTariffDataDto get(@PathVariable("transportTypeId") UUID transportTypeId, @PathVariable("tariffId") UUID tariffId);
    
    /**
     * Получение всех тарифов.
     *
     * @param regionId Идентификатор региона для фильтрации.
     * @param contractType тип договора.
     *
     * @return тарифы.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех тарифов")
    List<? extends ShortTariffDto> getAll(
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId,
            @RequestParam(name = "contractType", defaultValue = "TRANSITIONAL") @NotNull ContractType contractType
                                         );
    
    /**
     * Получение всех тарифов.
     *
     * @param typeId Идентификатор вида транспорта.
     * @param regionId Идентификатор региона для фильтрации.
     * @param contractType тип договора.
     *
     * @return тарифы.
     */
    @GetMapping(value = {"{transportTypeId}","{transportTypeId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение тарифов вида ТС",
               description = "Получение данных всех тарифов одного вида транспорта")
    List<? extends ShortTariffDto> getAll(
            @Parameter(description = "Идентификатор вида транспорта")
            @PathVariable("transportTypeId") UUID typeId,
            @Parameter(description = "Идентификатор региона для фильтрации")
            @RequestParam(value = "regionId", required = false) UUID regionId,
            @RequestParam(name = "contractType", defaultValue = "TRANSITIONAL") @NotNull ContractType contractType
                                         );
    
    /**
     * Поиск тарифа.
     *
     * @param searchDTO данные для поиска.
     * @param page страница.
     *
     * @return тарифы.
     */
    @PostMapping(value = {"search","search/"}, produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск тарифа по фильтру", description = "Поиск тарифа по фильтру")
    Page<? extends ShortTariffDto> search(
            @RequestBody(required = false) TariffSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = { "humanReadableId" }, direction =
                    Sort.Direction.ASC) Pageable page,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                         );
    
    
    /**
     * Переотправка тарифов через брокер.
     */
    @PutMapping(value = {"resendTariffs","resendTariffs/"})
    @Operation(summary = "Обновление данных всех тарифов в других сервисах",
               description = "Обновление данных всех тарифов в других сервисах")
    void resendTariffs();
    
    
    @GetMapping(value = {"workgroups","workgroups/"})
    @Operation(summary = "Получить список рабочих групп")
    List<WorkGroupFileDto> workgroups();
}
