package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationCountingService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка сервиса количественных триггеров уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationCountingServiceTest {

    private final NotificationSettingsRepository notificationSettingsRepository =
            mock(NotificationSettingsRepository.class);

    private final CountingRepository countingRepository = mock(CountingRepository.class);

    private final NotificationCountingService notificationCountingService =
            new NotificationCountingServiceImpl(notificationSettingsRepository, countingRepository);

    @DisplayName("Проверка получения данных")
    @Test
    void test_get() {
        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 3)));

        var actualCollection = notificationCountingService.getCountingOf(UUID.randomUUID(), UUID.randomUUID());

        assertThat(actualCollection).hasSize(3);

        var i = 0;
        for (var actual : actualCollection) {
            assertThat(actual.getPropertyName()).isEqualTo("property " + i);
            assertThat(actual.getCount()).isEqualTo(i * 10.0);
            assertThat(actual.getType()).isEqualTo(CountingType.values()[i]);
            i++;
        }
    }

    @DisplayName("Проверка получения данных. Нет уведомления")
    @Test
    void test_get_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        var id = UUID.randomUUID();
        try {
            notificationCountingService.getCountingOf(UUID.randomUUID(), id);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }

    @DisplayName("Проверка установки данных")
    @Test
    void test_set() {
        var captor = ArgumentCaptor.forClass(CountingSettings.class);

        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 0)));

        notificationCountingService.setCountingOf(UUID.randomUUID(), UUID.randomUUID(), createCountings(null, 0, 3));

        verify(countingRepository, times(3)).save(captor.capture());
        verify(countingRepository).deleteAllByIdInBatch(anyCollection());
        var captured = captor.getAllValues();

        assertThat(captured).hasSize(3);
        for (var i = 0; i < captured.size(); i++) {
            assertThat(captured.get(i).getPropertyName()).isEqualTo("property " + i);
            assertThat(captured.get(i).getType()).isEqualTo(CountingType.values()[i % 3]);
            assertThat(captured.get(i).getCount()).isEqualTo(i * 10D);
        }
    }

    @DisplayName("Проверка установки данных. Нет уведомления")
    @Test
    void test_set_noNotification() {
        when(notificationSettingsRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        var id = UUID.randomUUID();
        try {
            notificationCountingService.setCountingOf(UUID.randomUUID(), id, Collections.emptyList());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }

    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_editCounting() {
        var captor = ArgumentCaptor.forClass(CountingSettings.class);
        var deleteCaptor = ArgumentCaptor.forClass(List.class);

        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 1, 2)));

        notificationCountingService.setCountingOf(UUID.randomUUID(), UUID.randomUUID(), createCountings(null, 2, 3));

        verify(countingRepository, times(3)).save(captor.capture());
        verify(countingRepository).deleteAllByIdInBatch(deleteCaptor.capture());
        var captured = captor.getAllValues().stream()
                .sorted(Comparator.comparing(CountingSettings::getPropertyName)).toList();
        var deleted = deleteCaptor.getValue();
        assertThat(deleted).hasSize(2);

        assertThat(captured).hasSize(3);
        for (var i = 2; i < 2 + captured.size(); i++) {
            assertThat(captured.get(i - 2).getPropertyName()).isEqualTo("property " + i);
            assertThat(captured.get(i - 2).getType()).isEqualTo(CountingType.values()[i % 3]);
            assertThat(captured.get(i - 2).getCount()).isEqualTo(i * 10D);
        }
    }

    @DisplayName("Проверка установки данных. Редактирование")
    @Test
    void test_set_deleteCounting() {
        var captor = ArgumentCaptor.forClass(CountingSettings.class);

        when(notificationSettingsRepository.findByParentIdAndId(any(UUID.class), any(UUID.class)))
                .then(inv -> Optional.of(createNotification(inv.getArgument(0), 0, 3)));

        notificationCountingService.setCountingOf(UUID.randomUUID(), UUID.randomUUID(), createCountings(null, 0, 0));

        verify(countingRepository, never()).save(captor.capture());
        verify(countingRepository).deleteAllByIdInBatch(anyCollection());
    }

    private NotificationSettings createNotification(UUID id, int startIndex, int count) {
        var notification = new NotificationSettings();
        notification.setId(id);
        notification.setDescription("Description " + id);
        notification.setName("Name " + id);
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.getCountings().addAll(createCountings(notification, startIndex, count));
        return notification;
    }

    private List<CountingSettings> createCountings(NotificationSettings notification, int startIndex, int count) {
        var result = new ArrayList<CountingSettings>();
        for (var i = startIndex; i < startIndex + count; i++) {
            var counting = new CountingSettings();
            counting.setCount(i * 10.0);
            counting.setNotification(notification);
            counting.setPropertyName("property " + i);
            counting.setType(CountingType.values()[i % 3]);
            result.add(counting);
        }
        return result;
    }

}