package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.DefaultNotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.notification.NewNotificationSettingsDto;
import ru.sber.transport.notifications.dto.notification.NotificationSettingsDto;
import ru.sber.transport.notifications.mapper.settings.NotificationSettingsMapper;
import ru.sber.transport.notifications.services.DefaultNotificationSettingsService;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация работы с настройками.
 */
@RequiredArgsConstructor
@Transactional
@Component
@Slf4j
public class NotificationSettingsServiceImpl implements NotificationSettingsService {

    private final NotificationSettingsRepository repository;

    private final NotificationService notificationService;

    private final DefaultNotificationSettingsService defaultNotificationSettingsService;

    private final NotificationSettingsMapper mapper;

    private final OrganizationRepository organizationRepository;

    private final DefaultNotificationSettingsRepository defaultNotificationSettingsRepository;

    @Override
    public NotificationSettings add(
            UUID organizationId, NotificationSettings data, UUID ownerId
    ) {
        checkUnique(organizationId, data);
        data.setOwnerId(null);
        data.setParentId(organizationId);
        data.getChannels().forEach(item -> item.setNotification(data));
        data.getTimings().forEach(item -> item.setNotification(data));
        data.getCountings().forEach(item -> item.setNotification(data));
        var restrictions = data.getRestrictions();
        if (restrictions != null) {
            restrictions.setNotification(data);
            restrictions.getRoles().forEach(role -> role.setRestrictionSettings(restrictions));
        }
        return repository.save(data);
    }

    @Override
    public NotificationSettings edit(UUID organizationId, UUID id, NotificationSettings data) {
        var notification = get(organizationId, id);
        return edit(organizationId, data, id, notification);
    }

    @Override
    public void editDispatcherNotification(UUID organizationId, UUID ownerId, UUID id, NotificationSettings data) {
        var notification = getDispatcherNotification(ownerId, id);
        edit(organizationId, data, id, notification);
    }

    private NotificationSettings edit(UUID organizationId, NotificationSettings data, UUID id, NotificationSettings notification) {
        checkUnique(organizationId, data, id);
        data.getChannels().forEach(ch -> ch.setNotification(notification));
        data.getTimings().forEach(timing -> timing.setNotification(notification));
        data.getCountings().forEach(countingSettings -> countingSettings.setNotification(notification));
        mapper.update(notification, data);
        notification.setParentId(organizationId);
        return repository.save(notification);
    }

    @Override
    public void delete(UUID organizationId, UUID id) {
        getAll(organizationId).stream().filter(s -> s.getId() != null).forEach(ns -> {
            notificationService.cancelAll(ns);
            ns.getChannels().clear();
            ns.getTimings().clear();
            ns.getCountings().clear();
            if (ns.getRestrictions() != null) {
                ns.getRestrictions().getRoles().clear();
                ns.setRestrictions(null);
            }
            repository.delete(ns);
        });
    }

    @Override
    public void deleteDispatcherNotification(UUID ownerId, UUID id) {
        var notificationSettings = getAllDispatcherNotifications(ownerId);
        notificationSettings.forEach(ns -> {
            notificationService.cancelAll(ns);
            ns.getChannels().clear();
            ns.getTimings().clear();
            ns.getCountings().clear();
            if (ns.getRestrictions() != null) {
                ns.getRestrictions().getRoles().clear();
                ns.setRestrictions(null);
            }
            repository.delete(ns);
        });
    }

    @Override
    public NotificationSettings get(UUID organizationId, UUID id) {
        return repository.findByParentIdAndId(organizationId, id)
                .orElseThrow(() -> new EntityNotFoundException(NotificationSettings.class, id));
    }

    @Override
    public NotificationSettings getDispatcherNotification(UUID ownerId, UUID id) {
        return repository.findByOwnerIdAndIdAndNotificationClass(ownerId, id, NotificationClass.DISPATCHER_NOTIFICATION)
                .orElseThrow(() -> new EntityNotFoundException(NotificationSettings.class, id));
    }

    @Override
    public NotificationSettings get(UUID parentId, NotificationClass notificationClass,
                                    NotificationType type) {
        var settingsList =
                repository.findByParentIdAndNotificationClassAndTypeAndOwnerId(parentId, notificationClass,
                        type, null);
        return !settingsList.isEmpty() ? settingsList.getFirst() :
                add(parentId, defaultNotificationSettingsService.getSettingByClassAndType(notificationClass, type), null);
    }

    @Override
    public List<NotificationSettings> getAll(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        var found = repository.findAllByParentIdOrderByName(organizationId);
        if (found.isEmpty()) {
            return defaultNotificationSettingsRepository.findAllDefaultSettings();
        }
        return found;
    }

    @Override
    public List<NotificationSettings> getAllDispatcherNotifications(UUID ownerId) {
        var notificationSettings = repository.findAllByOwnerIdAndNotificationClassOrderByName(ownerId, NotificationClass.DISPATCHER_NOTIFICATION);
        if (notificationSettings.isEmpty()) {
            throw new EntityNotFoundException(NotificationSettings.class, "ownerId " + ownerId);
        }
        return notificationSettings;
    }

    @Override
    public void addSettingsForNewOrganization(UUID organizationId) {
        defaultNotificationSettingsService.getDefaultSettings()
                .forEach(setting -> add(organizationId, new NotificationSettings(setting), null));
    }

    private void checkUnique(UUID organizationId, NotificationSettings data) {
        checkUnique(organizationId, data, null);
    }

    private void checkUnique(UUID organizationId, NotificationSettings data, UUID id) {
        var notificationDesc = data.getDescription();
        var settings = id == null
                ? repository.findByParentIdAndDescription(organizationId, notificationDesc)
                : repository.findByParentIdAndDescriptionAndIdNot(organizationId, notificationDesc, id);
        if (settings.isPresent()) {
            throw new DuplicateDataException(NotificationSettings.class, Map.of("description", notificationDesc, "parent_id", organizationId));
        }
    }

    @Transactional
    public NotificationSettings toModel(NewNotificationSettingsDto dto) {
        return mapper.toModel(dto);
    }

    @Transactional
    public NotificationSettingsDto toDto(NotificationSettings dto) {
        return mapper.toDto(dto);
    }

    @Transactional
    public NotificationSettings toModel(NotificationSettingsDto dto) {
        return mapper.toModel(dto);
    }
}
