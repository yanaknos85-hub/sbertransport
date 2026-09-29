package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.request.dto.CheckinSettingsDTO;

import java.util.Collection;
import java.util.UUID;

/**
 * Controller interface for checkin settingss.
 */
@RequestMapping(value = {"checkinsettings","checkinsettings/"})
@Tag(name = "Настройки чекин", description = "Настройки чекин")
public interface CheckinSettingsController {
    
    /**
     * Add a new checkin setting.
     *
     * @param CheckinSettingsDTO new checkin setting data.
     *
     * @return added checkin setting.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Сохранение значения настройки", description = "Сохранение значения настройки")
    CheckinSettingsDTO save(@Valid @RequestBody CheckinSettingsDTO CheckinSettingsDTO);
    
    /**
     * Delete checkin settings.
     *
     * @param id name of checkin setting to delete.
     */
    @DeleteMapping(value = {"{id}","{id}/"})
    @Operation(summary = "Удаление настройки", description = "Удаление настройки")
    void delete(@PathVariable("id") UUID id);
    
    /**
     * Get checkin settings with ID.
     *
     * @param id ID of checkin settings to get.
     *
     * @return checkin settings.
     */
    @GetMapping(value = {"{id}","{id}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки", description = "Получение настройки")
    CheckinSettingsDTO get(@PathVariable("id") @NotNull UUID id);
    
    /**
     * Get all checkin settingss.
     *
     * @return list of checkin settingss.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех настроек", description = "Получение всех настроек")
    Collection<? extends CheckinSettingsDTO> getAll();
}
