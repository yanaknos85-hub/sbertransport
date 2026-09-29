package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.services.NotificationRestrictionsService;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.RoleRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionRoles;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;

import java.util.UUID;

/**
 * Реализация сервиса для работы сограничениями.
 */
@RequiredArgsConstructor
@Transactional
@Component
class NotificationRestrictionsServiceImpl implements NotificationRestrictionsService {
    
    private final NotificationSettingsRepository notificationSettingsRepository;
    
    private final RoleRepository roleRepository;
    
    @Override
    public RestrictionSettings getRestrictionsOf(
            UUID organizationId, UUID id
                                                ) {
        return notificationSettingsRepository.findByParentIdAndId(organizationId, id)
                                             .map(NotificationSettings::getRestrictions)
                                             .orElseThrow(() -> new EntityNotFoundException(Notification.class, id));
    }

    @SuppressWarnings("java:S3958")
    @Override
    public void setRestrictionsOf(
            UUID organizationId, UUID id,
            RestrictionSettings data
                                 ) {
        var notification = notificationSettingsRepository.findByParentIdAndId(organizationId, id)
                                                         .orElseThrow(
                                                                 () -> new EntityNotFoundException(Notification.class, id));
        
        var roles = data.getRoles().stream().map(RestrictionRoles::getRole).map(Role::getCode)
                .map(this::getRole).toList();
        data.getRoles().clear();
        data.getRoles().addAll(roles.stream().map(role -> getRestrictionSettings(data, role)).toList());
        
        notification.setRestrictions(null);
        data.setNotification(notification);
        notification.setRestrictions(data);
        notificationSettingsRepository.save(notification);
    }
    
    public RestrictionRoles getRestrictionSettings(RestrictionSettings data, Role role) {
        var roles = new RestrictionRoles();
        roles.setRole(role);
        roles.setRestrictionSettings(data);
        return roles;
    }
    
    private Role getRole(String role) {
        return roleRepository.findById(role).orElseThrow(() -> new EntityNotFoundException(Role.class, role));
    }
}
