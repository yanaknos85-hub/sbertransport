package ru.sber.transport.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;
import ru.sber.transport.notifications.enums.UserNotificationSettingsSortOption;

import java.util.UUID;

@Tag(name = "Пользовательские настройки уведомлений", description = "Пользовательские настройки уведомлений для личного кабинета")
@RequestMapping("/user/settings")
public interface UserNotificationSettingsController {

    @PostMapping
    @ResponseBody
    @Operation(summary = "Поиск настроек уведомлений для пользователей", description = "Поиск настроек уведомлений для пользователей")
    Page<UserNotificationSettingsDto> search(@RequestBody UserNotificationSettingsSearchDto searchDto);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить настройки уведомлений для пользователя", description = "Получить настройки уведомлений для пользователя с фильтром по NotificationClass")
    Page<UserNotificationSettingsDto> getForUser(@Parameter(description = "Класс уведомлений, массив")
                                                 @RequestParam("class") NotificationClass notificationClass,
                                                 @Parameter(description = "Размер страницы")
                                                 @RequestParam(value = "pageSize") Integer size,
                                                 @Parameter(description = "Номер страницы")
                                                 @RequestParam(value = "page") Integer page,
                                                 @Parameter(description = "Направление сортировки")
                                                 @RequestParam(value = "directionAsc") Boolean directionAsc,
                                                 @Parameter(description = "Поле сортировки")
                                                 @RequestParam(value = "sortField") UserNotificationSettingsSortOption field,
                                                 @Parameter(hidden = true) JwtAuthenticationToken authentication);


    @PutMapping(value = "{id}")
    @ResponseBody
    @Operation(summary = "Изменить настройку уведомлений для пользователя", description = "Изменить настройку уведомления для пользователя")
    UserNotificationSettingsDto update(@PathVariable("id") UUID id, @RequestBody @Valid UserNotificationSettingsDto data);
}
