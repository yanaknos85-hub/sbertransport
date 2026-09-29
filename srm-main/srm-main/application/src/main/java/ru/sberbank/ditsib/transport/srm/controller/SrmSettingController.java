package ru.sberbank.ditsib.transport.srm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.dto.SrmSettingDTO;

import java.util.Collection;

/**
 * Controller interface for srm settingss.
 */
@RequestMapping(value = {"srmsettings","srmsettings/"})
@Tag(name = "Глобальные настройки совместных поездок",
     description = "Глобальные настройки совместных поездок")
public interface SrmSettingController {
    
    /**
     * Add a new srm settings.
     *
     * @param srmSettingDTO new srm settings data.
     *
     * @return added srm.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Сохранение значения настройки", description = "Сохранение значения настройки")
    SrmSettingDTO save(@Valid @RequestBody SrmSettingDTO srmSettingDTO);

    /**
     * Delete srm settings.
     *
     * @param srmSettingsName name of srm sharing to delete.
     */
    @DeleteMapping(value = {"{srmSettingsName}","{srmSettingsName}/"})
    @Operation(summary = "Удаление настройки", description = "Удаление настройки")
    void delete(@PathVariable("srmSettingsName") SrmSettingNames srmSettingsName);
    
    /**
     * Get srm settings with ID.
     *
     * @param srmSettingsName ID of srm settings to get.
     *
     * @return srm settings.
     */
    @GetMapping(value = {"{srmSettingsName}","{srmSettingsName}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки", description = "Получение настройки")
    SrmSettingDTO get(@PathVariable("srmSettingsName") @NotNull SrmSettingNames srmSettingsName);
    
    /**
     * Get srm settings with ID.
     *
     * @param srmSettingsName ID of srm settings to get.
     *
     * @return srm settings.
     */
    @GetMapping(value = {"getByName/{srmSettingsName}","getByName/{srmSettingsName}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки", description = "Получение настройки")
    String getByName(@PathVariable("srmSettingsName") @NotNull SrmSettingNames srmSettingsName);
    
    /**
     * Get all srm settingss.
     *
     * @return list of srm settingss.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех настроек", description = "Получение всех настроек")
    Collection<? extends SrmSettingDTO> getAll();
}
