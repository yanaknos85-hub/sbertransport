package ru.sber.transport.notifications.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.notifications.controller.NotificationSettingsController;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.dto.notification.NewNotificationSettingsDto;
import ru.sber.transport.notifications.dto.notification.NotificationSettingsDto;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера для работы с настройками.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@RestController
class NotificationSettingsControllerImpl implements NotificationSettingsController {

    private final NotificationSettingsService settingsService;
    private final EmployeeService employeeService;
    private final UserNotificationSettingsService userNotificationSettingsService;

    @CheckOrganizationAccess
    @Override
    public NotificationSettingsDto add(@Organization UUID organizationId, NewNotificationSettingsDto newData,
                                       JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        final NotificationSettings newSettings = settingsService.add(organizationId, settingsService.toModel(newData), employee.getId());

        userNotificationSettingsService.saveByEngineerCorpClient(newSettings);

        return settingsService.toDto(newSettings);
    }

    @CheckOrganizationAccess
    @Override
    public void edit(
            @Organization UUID organizationId, UUID id,
            @Valid NewNotificationSettingsDto newData,
            JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        final NotificationSettings newSettings = settingsService.edit(organizationId, id, settingsService.toModel(newData));
        userNotificationSettingsService.saveByEngineerCorpClient(newSettings);
        log.info("UPDATE_SETTINGS: user ID {} update notification settings ID {}, organization ID {}", employee.getId(), newSettings.getId(), organizationId );
    }

    @CheckOrganizationAccess
    @Override
    public void editDispatcherNotification(@Organization UUID organizationId, UUID ownerId, UUID id,
                                           @Valid NewNotificationSettingsDto newData) {
        settingsService.editDispatcherNotification(organizationId, ownerId, id, settingsService.toModel(newData));
    }

    @SuppressWarnings("java:S3958")
    @CheckOrganizationAccess
    @Override
    public List<NotificationSettingsDto> add(
            @Organization UUID organizationId,
            List<@Valid NewNotificationSettingsDto> newData, JwtAuthenticationToken authentication
    ) {
        return newData.stream().map(newItem -> add(organizationId, newItem, authentication)).toList();
    }


    @CheckOrganizationAccess
    @Override
    public void edit(
            @Organization UUID organizationId,
            List<@Valid NotificationSettingsDto> newData
    ) {
        newData.stream().map(settingsService::toModel).forEach(item -> settingsService.edit(organizationId, item.getId(), item));
    }

    @CheckOrganizationAccess
    @Override
    public void editDispatcherNotifications(@Organization UUID organizationId, UUID ownerId,
                                            List<@Valid NotificationSettingsDto> newData) {
        newData.stream().map(settingsService::toModel).forEach(item -> settingsService.editDispatcherNotification(organizationId, ownerId,
                item.getId(),
                item));
    }

    @CheckOrganizationAccess
    @Override
    public void delete(@Organization UUID organizationId, UUID id) {
        settingsService.delete(organizationId, id);
    }

    @Override
    public void deleteDispatcherNotification(UUID ownerId, UUID id) {
        settingsService.deleteDispatcherNotification(ownerId, id);
    }

    @CheckOrganizationAccess
    @Override
    public NotificationSettingsDto get(@Organization UUID organizationId, UUID id) {
        return settingsService.toDto(settingsService.get(organizationId, id));
    }

    @CheckOrganizationAccess
    @Override
    public NotificationSettingsDto getDispatcherNotifications(@Organization UUID organizationId, UUID ownerId, UUID id) {
        return settingsService.toDto(settingsService.getDispatcherNotification(ownerId, id));
    }

    @CheckOrganizationAccess
    @Override
    @SuppressWarnings("java:S3958")
    public Collection<NotificationSettingsDto> getAll(@Organization UUID organizationId) {
        return settingsService.getAll(organizationId).stream().map(settingsService::toDto).toList();
    }

}
