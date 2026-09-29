package ru.sber.transport.notifications.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.notifications.controller.UserNotificationSettingsController;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;
import ru.sber.transport.notifications.enums.UserNotificationSettingsSortOption;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserNotificationSettingsControllerImpl implements UserNotificationSettingsController {

    private final UserNotificationSettingsService service;
    private final EmployeeService employeeService;

    @Override
    public Page<UserNotificationSettingsDto> search(UserNotificationSettingsSearchDto searchDto) {
        return service.search(searchDto);
    }

    @Override
    public Page<UserNotificationSettingsDto> getForUser(NotificationClass notificationClass, Integer size, Integer page,
                                                        Boolean directionAsc, UserNotificationSettingsSortOption field,
                                                        JwtAuthenticationToken authentication) {
        var searchInitiatorEmployee = employeeService.getAuthenticatedEmployee(authentication);
        var search = UserNotificationSettingsSearchDto.builder()
                .userId(searchInitiatorEmployee.getId())
                .parentId(searchInitiatorEmployee.getOrganizationId())
                .notificationClass(Set.of(notificationClass))
                .pageSetting(UserNotificationSettingsSearchDto.PageSetting.builder().page(page).size(size).build())
                .sortSetting(UserNotificationSettingsSearchDto.SortSetting.builder()
                        .property(field)
                        .directionAsc(directionAsc)
                        .build())
                .build();

        return service.get(search);
    }

    @Override
    public UserNotificationSettingsDto update(UUID id, UserNotificationSettingsDto data) {
        return service.updateByUser(id, data);
    }
}
