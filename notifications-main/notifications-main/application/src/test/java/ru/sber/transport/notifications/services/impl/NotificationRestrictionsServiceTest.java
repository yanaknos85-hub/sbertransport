package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.RoleRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionRoles;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;
import ru.sber.transport.notifications.services.NotificationRestrictionsService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Проверка сервиса ограничений уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationRestrictionsServiceTest {
    
    private final NotificationSettingsRepository notificationSettingsRepository =
            mock(NotificationSettingsRepository.class);
    
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    
    private final NotificationRestrictionsService notificationTimingService =
            new NotificationRestrictionsServiceImpl(notificationSettingsRepository, roleRepository);
    
    @DisplayName("Проверка получения данных")
    @Test
    void test_get() {
        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), RestrictType.ALLOW_BUT, 0, 3)));
        
        var actual = notificationTimingService.getRestrictionsOf(UUID.randomUUID(),
                                                                 UUID.randomUUID());
        
        assertThat(actual.getRestrictType()).isEqualTo(RestrictType.ALLOW_BUT);
        assertThat(actual.getRoles()).hasSize(3);
        
        var i = 0;
        for (var role : actual.getRoles()) {
            assertThat(role.getRole().getCode()).isEqualTo("Code " + i);
            i++;
        }
    }
    
    @DisplayName("Проверка получения данных. Нет уведомления")
    @Test
    void test_get_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        var id = UUID.randomUUID();
        try {
            notificationTimingService.getRestrictionsOf(UUID.randomUUID(), id);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "Notification")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }
    
    @DisplayName("Проверка установки данных")
    @Test
    void test_set() {
        var notification = createNotification(null, RestrictType.DENY_ALL, 0, 0);

        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(notification));
        when(roleRepository.findById(anyString()))
                .then(inv -> Optional.of(createRole(inv.getArgument(0))));
    
        notificationTimingService.setRestrictionsOf(UUID.randomUUID(), UUID.randomUUID(),
                                                    createRestriction(notification, RestrictType.DENY_BUT,
                                                                      0, 3));
    
        var captured = notification.getRestrictions();
    
        assertThat(captured.getRestrictType()).isEqualTo(RestrictType.DENY_BUT);
        assertThat(captured.getRoles()).hasSize(3);
        for (var i = 0; i < captured.getRoles().size(); i++) {
            assertThat(captured.getRoles().get(i).getRole().getCode()).isEqualTo("Code " + i);
        }
    }

    @DisplayName("Проверка установки данных. Нет уведомления")
    @Test
    void test_set_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        var id = UUID.randomUUID();
        try {
            notificationTimingService.setRestrictionsOf(UUID.randomUUID(), id, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "Notification")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }
    
    @DisplayName("Проверка установки данных. Нет роли")
    @Test
    void test_set_noRole() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        var id = UUID.randomUUID();
        try {
            notificationTimingService.setRestrictionsOf(UUID.randomUUID(), id, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "Notification")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }
    
    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_editCounting() {
        var notification = createNotification(null, RestrictType.DENY_BUT, 1, 2);
        
        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(notification));
        when(roleRepository.findById(anyString()))
                .then(inv -> Optional.of(createRole(inv.getArgument(0))));
        
        notificationTimingService
                .setRestrictionsOf(UUID.randomUUID(), UUID.randomUUID(), createRestriction(notification,
                                                                                           RestrictType.ALLOW_BUT, 2,
                                                                                           3));
        
        var captured = notification.getRestrictions();
        
        assertThat(captured.getRestrictType()).isEqualTo(RestrictType.ALLOW_BUT);
        assertThat(captured.getRoles()).hasSize(3);
        for (var i = 2; i < 2 + captured.getRoles().size(); i++) {
            assertThat(captured.getRoles().get(i - 2).getRole().getCode()).isEqualTo("Code " + i);
        }
    }
    
    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_deleteCounting() {
        var notification = createNotification(null, RestrictType.DENY_BUT, 0, 3);
    
        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(notification));
    
        notificationTimingService
                .setRestrictionsOf(UUID.randomUUID(), UUID.randomUUID(), createRestriction(notification,
                                                                                           RestrictType.ALLOW_BUT,
                                                                                           0, 0));
    
        var captured = notification.getRestrictions();
    
        assertThat(captured.getRestrictType()).isEqualTo(RestrictType.ALLOW_BUT);
        assertThat(captured.getRoles()).isEmpty();
    }
    
    private NotificationSettings createNotification(UUID id, RestrictType restrictType, int startIndex, int count) {
        var notification = new NotificationSettings();
        notification.setId(id);
        notification.setDescription("Description " + id);
        notification.setName("Name " + id);
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setRestrictions(createRestriction(notification, restrictType, startIndex, count));
        return notification;
    }
    
    private RestrictionSettings createRestriction(NotificationSettings notification, RestrictType restrictType,
                                                  int startIndex, int count) {
        var settings = new RestrictionSettings();
        settings.setRestrictType(restrictType);
        settings.setNotification(notification);
        
        for (var i = startIndex; i < startIndex + count; i++) {
            var role = createRole("Code " + i);
            settings.getRoles().add(new RestrictionRoles(role));
        }
        return settings;
    }

    private Role createRole(String argument) {
        var role = new Role();
        role.setCode(argument);
        return role;
    }
    
}