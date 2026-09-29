package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.LIMIT_PERSON;

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Проверка сервиса настроек тайминга")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class TimingServiceImplTest {

    @Autowired
    private TimingRepository repository;
    
    @Autowired
    private NotificationSettingsRepository settingsRepository;
    
    @Autowired
    private TimingService service;
    
    @Test
    @DisplayName("Получение")
    void test_getBySettings() {
        var settings = new NotificationSettings();
        settings.setOwnerId(UUID.randomUUID());
        settings.setDescription("Description");
        settings.setName("Name");
        settings.setNotificationClass(LIMIT_PERSON);
        settings.setType(NotificationType.ALLOCATION);
        settings.setParentId(UUID.randomUUID());
        settings = settingsRepository.save(settings);
        
        var timingSettings = new TimingSettings();
        timingSettings.setTimeBefore(Duration.ZERO);
        timingSettings.setDeadlineFieldName("dealine");
        timingSettings.setType(EventType.AT_EVENT);
        timingSettings.setNotification(settings);
        repository.save(timingSettings);
        
        var actualList = service.getOfSettings(settingsRepository.findAll().get(0));
        assertThat(actualList).hasSize(1);
        assertThat(actualList.get(0).getDeadlineFieldName()).isEqualTo(timingSettings.getDeadlineFieldName());
        assertThat(actualList.get(0).getTimeBefore()).isEqualTo(timingSettings.getTimeBefore());
        assertThat(actualList.get(0).getNotification().getId()).isEqualTo(timingSettings.getNotification().getId());
        assertThat(actualList.get(0).getType()).isEqualTo(timingSettings.getType());
    }
    
}