package ru.sber.transport.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.notifications.dto.notification.NewNotificationSettingsDto;
import ru.sber.transport.notifications.dto.notification.NotificationSettingsDto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с настройками уведомлений.
 */
@RequestMapping({"/{organizationId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/settings/", "/{organizationId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/settings"})
@Validated
@Tag(name = "Настройки уведомлений", description = "Набор операций для работы с настройками уведомлений")
public interface NotificationSettingsController {
    
    /**
     * Добавление базовых настроек уведомлений.
     *
     * @param newData новые данные настроек.
     *
     * @return сохраненные настройки.
     */
    @Operation(summary = "Добавление", description = "Добавление новой базовой настройки уведомлений")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    NotificationSettingsDto add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody @Valid NewNotificationSettingsDto newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                               );
    
    /**
     * Изменение базовых настроек уведомлений.
     *
     * @param id идентификатор настроек.
     * @param newData новые данные настроек.
     */
    @Operation(summary = "Изменение", description = "Изменение базовых настроек уведомлений")
    @PutMapping(
            value = {"/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void edit(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") UUID id, @RequestBody @Valid NewNotificationSettingsDto newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
             );
    
    
    /**
     * Изменение настроек уведомлений диспетчерской.
     *
     * @param id идентификатор настроек.
     * @param newData новые данные настроек.
     */
    @Operation(summary = "Изменение", description = "Изменение настроек уведомлений диспетчерской")
    @PutMapping(
            value = {"/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void editDispatcherNotification(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("id") UUID id,
            @RequestBody @Valid NewNotificationSettingsDto newData
             );
    
    
    /**
     * Добавление базовых настроек уведомлений.
     *
     * @param newData новые данные настроек.
     *
     * @return сохраненные настройки.
     */
    @Operation(summary = "Массовое добавление", description = "Массовое добавление новых базовых настроек уведомлений")
    @PostMapping(value = {"/mass/", "/mass"}, produces = MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<NotificationSettingsDto> add(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody List<@Valid NewNotificationSettingsDto> newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                     );
    
    /**
     * Изменение базовых настроек уведомлений.
     *
     * @param newData новые данные настроек.
     */
    @Operation(summary = "Массовое изменение", description = "Массовое изменение базовых настроек уведомлений")
    @PutMapping(value = {"/mass/", "/mass"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    void edit(
            @PathVariable("organizationId") UUID organizationId,
            @RequestBody List<@Valid NotificationSettingsDto> newData
             );
    
    /**
     * Изменение настроек уведомлений диспетчерской.
     *
     * @param newData новые данные настроек.
     */
    @Operation(summary = "Массовое изменение", description = "Массовое изменение настроек уведомлений диспетчерской")
    @PutMapping(
            value = {"/mass/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/mass/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void editDispatcherNotifications(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("employeeId") UUID employeeId,
            @RequestBody List<@Valid NotificationSettingsDto> newData
             );
    
    
    /**
     * Удаление базовых настроек.
     *
     * @param id идентификатор настроек.
     */
    @Operation(summary = "Удаление", description = "Удаление настроек уведомлений")
    @DeleteMapping(value = {"/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"})
    void delete(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") UUID id
               );
    
    /**
     * Удаление настроек уведомлений диспетчерской.
     *
     * @param id идентификатор настроек.
     */
    @Operation(summary = "Удаление", description = "Удаление настроек уведомлений диспетчерской")
    @DeleteMapping(value = {"/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"})
    void deleteDispatcherNotification(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("id") UUID id
               );
    
    
    /**
     * Получение базовых настроек.
     *
     * @param id идентификатор настроек.
     *
     * @return настройки.
     */
    @Operation(summary = "Получение", description = "Получение базовых настроек уведомлений")
    @GetMapping(value = {"/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    NotificationSettingsDto get(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("id") UUID id
                               );
    
    /**
     * Получение настроек уведомлений диспетчерской.
     *
     * @param id идентификатор настроек.
     *
     * @return настройки.
     */
    @Operation(summary = "Получение", description = "Получение настроек уведомлений диспетчерской.")
    @GetMapping(value = {"/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/", "/{employeeId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/{id:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    NotificationSettingsDto getDispatcherNotifications(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("id") UUID id
                               );
    
    /**
     * Получение всех базовых настроек.
     *
     * @return список настроек.
     */
    @Operation(summary = "Получение всех", description = "Получение всех базовых настроек уведомлений")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Collection<NotificationSettingsDto> getAll(@PathVariable("organizationId") UUID organizationId);
    
}
