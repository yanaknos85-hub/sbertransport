package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.NotificationTimingService;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка сервиса тайминга уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationTimingServiceTest {
    
    private final NotificationSettingsRepository notificationSettingsRepository =
            mock(NotificationSettingsRepository.class);
    
    private final TimingRepository timingRepository = mock(TimingRepository.class);
    
    private final NotificationTimingService notificationTimingService =
            new NotificationTimingServiceImpl(notificationSettingsRepository, timingRepository);
    
    @DisplayName("Проверка получения данных")
    @Test
    void test_get() {
        when(notificationSettingsRepository.findById(any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 3)));
        
        var actualCollection = notificationTimingService.getTimingOf(UUID.randomUUID(), UUID.randomUUID());
        
        assertThat(actualCollection).hasSize(3);
        
        var i = 0;
        for (var actual : actualCollection) {
            assertThat(actual.getTimeBefore()).isEqualTo(Duration.ZERO.plusMillis(i));
            assertThat(actual.getType().name()).isEqualTo(EventType.values()[i % EventType.values().length].name());
            i++;
        }
    }
    
    @DisplayName("Проверка получения данных. Нет уведомления")
    @Test
    void test_get_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        var id = UUID.randomUUID();
        try {
            notificationTimingService.getTimingOf(UUID.randomUUID(), id);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }
    
    @DisplayName("Проверка установки данных")
    @Test
    void test_set() {
        var captor = ArgumentCaptor.forClass(TimingSettings.class);
    
        when(notificationSettingsRepository.findById(any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 0)));
    
        notificationTimingService.setTimingOf(UUID.randomUUID(), UUID.randomUUID(), createTimings(null, 0, 3));
    
        verify(timingRepository, times(3)).save(captor.capture());
        verify(timingRepository).deleteAllByIdInBatch(anyCollection());
        var captured = captor.getAllValues();
    
        assertThat(captured).hasSize(3);
        for (var i = 0; i < captured.size(); i++) {
            assertThat(captured.get(i).getTimeBefore()).isEqualTo(Duration.ZERO.plusMillis(i));
            assertThat(captured.get(i).getType().name())
                    .isEqualTo(EventType.values()[i % EventType.values().length].name());
        }
    }
    
    @DisplayName("Проверка установки данных. Нет уведомления")
    @Test
    void test_set_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        var id = UUID.randomUUID();
        try {
            notificationTimingService.setTimingOf(UUID.randomUUID(), id, Collections.emptyList());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }
    
    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_editCounting() {
        var captor = ArgumentCaptor.forClass(TimingSettings.class);
        var deleteCaptor = ArgumentCaptor.forClass(List.class);
    
        when(notificationSettingsRepository.findById(any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 1, 2)));
    
        notificationTimingService.setTimingOf(UUID.randomUUID(), UUID.randomUUID(), createTimings(null, 2, 3));
    
        verify(timingRepository, times(3)).save(captor.capture());
        verify(timingRepository).deleteAllByIdInBatch(deleteCaptor.capture());
        var captured = new ArrayList<>(captor.getAllValues());
        var deleted = deleteCaptor.getValue();
        assertThat(deleted).hasSize(2);
    
        assertThat(captured).hasSize(3);
        for (var i = 2; i < 2 + captured.size(); i++) {
            assertThat(captured.get(i - 2).getTimeBefore()).isEqualTo(Duration.ZERO.plusMillis(i));
            assertThat(captured.get(i - 2).getType().name())
                    .isEqualTo(EventType.values()[i % EventType.values().length].name());
        }
    }
    
    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_deleteCounting() {
        var captor = ArgumentCaptor.forClass(TimingSettings.class);
    
        when(notificationSettingsRepository.findById(any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 3)));
    
        notificationTimingService.setTimingOf(UUID.randomUUID(), UUID.randomUUID(), createTimings(null, 0, 0));
    
        verify(timingRepository, never()).save(captor.capture());
        verify(timingRepository).deleteAllByIdInBatch(anyCollection());
    }
    
    private NotificationSettings createNotification(UUID id, int startIndex, int count) {
        var notification = new NotificationSettings();
        notification.setId(id);
        notification.setDescription("Description " + id);
        notification.setName("Name " + id);
        notification.setType(NotificationType.ALLOCATION);
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.getTimings().addAll(createTimings(notification, startIndex, count));
        return notification;
    }
    
    private List<TimingSettings> createTimings(NotificationSettings notification, int startIndex, int count) {
        var result = new ArrayList<TimingSettings>();
        for (var i = startIndex; i < startIndex + count; i++) {
            var counting = new TimingSettings();
            counting.setNotification(notification);
            counting.setTimeBefore(Duration.ZERO.plusMillis(i));
            counting.setType(EventType.values()[i % EventType.values().length]);
            result.add(counting);
        }
        return result;
    }
    
}