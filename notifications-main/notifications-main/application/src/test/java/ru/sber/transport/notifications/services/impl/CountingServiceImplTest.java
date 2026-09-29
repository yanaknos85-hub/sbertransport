package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.CountingService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.LIMIT_PERSON;

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Проверка сервиса количественных настроек")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class CountingServiceImplTest {
    
    @Autowired
    private CountingRepository repository;
    
    @Autowired
    private NotificationSettingsRepository settingsRepository;
    
    @Autowired
    private CountingService service;
    
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
        
        var countingSettings = new CountingSettings();
        countingSettings.setInitialPropertyName("Initial property");
        countingSettings.setPropertyName("property");
        countingSettings.setType(CountingType.EXACT);
        countingSettings.setCount(1D);
        countingSettings.setNotification(settings);
        repository.save(countingSettings);
        
        var actualList = service.getOfSettings(settingsRepository.findAll().get(0));
        assertThat(actualList).hasSize(1);
        assertThat(actualList.get(0).getNotification().getId()).isEqualTo(countingSettings.getNotification().getId());
        assertThat(actualList.get(0).getInitialPropertyName()).isEqualTo(countingSettings.getInitialPropertyName());
        assertThat(actualList.get(0).getPropertyName()).isEqualTo(countingSettings.getPropertyName());
        assertThat(actualList.get(0).getType()).isEqualTo(countingSettings.getType());
        assertThat(actualList.get(0).getCount()).isEqualTo(countingSettings.getCount());
    }
}