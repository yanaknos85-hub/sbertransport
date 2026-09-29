package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DefaultNotificationSettingsService;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;

import jakarta.transaction.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@EmbeddedPostgres
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Проверка сервиса настроек")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationSettingsServiceTest {

    @Autowired
    private NotificationSettingsRepository repository;

    @Autowired
    private NotificationSettingsService service;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private DefaultNotificationSettingsService defaultNotificationSettingsService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    @DisplayName("Добавление")
    void test_add() {
        var data = new NotificationSettings();
        data.setDescription("Description");
        data.setName("Name");
        data.setNotificationClass(NotificationClass.LIMIT_PERSON);
        data.setType(NotificationType.ALLOCATION);
        data.getChannels().addAll(createChannelSettings(data));

        var saved = service.add(UUID.randomUUID(), data, UUID.randomUUID());

        assertThat(saved.getName()).isEqualTo(data.getName());
        assertThat(saved.getDescription()).isEqualTo(data.getDescription());
        assertThat(saved.getType()).isEqualTo(data.getType());
        assertThat(saved.getChannels()).hasSameSizeAs(data.getChannels());
        assertThat(saved.getChannels().getFirst().getText()).isEqualTo(data.getChannels().getFirst().getText());
        assertThat(saved.getChannels().getFirst().getChannel()).isEqualTo(data.getChannels().getFirst().getChannel());
        assertThat(saved.getChannels().get(1).getText()).isEqualTo(data.getChannels().get(1).getText());
        assertThat(saved.getChannels().get(1).getChannel()).isEqualTo(data.getChannels().get(1).getChannel());
        assertThat(saved.getChannels().get(2).getText()).isEqualTo(data.getChannels().get(2).getText());
        assertThat(saved.getChannels().get(2).getChannel()).isEqualTo(data.getChannels().get(2).getChannel());
    }

    @Test
    @DisplayName("Добавление дубликата")
    void test_add_duplicate() {
        var organizationId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();

        var value = new NotificationSettings();
        value.setName("Name");
        value.setNotificationClass(NotificationClass.LIMIT_PERSON);
        value.setType(NotificationType.ALLOCATION);
        value.setParentId(organizationId);
        value.setOwnerId(ownerId);
        value.setDescription("Description");
        value = repository.save(value);

        var data = new NotificationSettings();
        data.setDescription("Description");
        data.setName("Name");
        data.setNotificationClass(NotificationClass.LIMIT_PERSON);
        data.setType(NotificationType.ALLOCATION);
        data.setOwnerId(ownerId);
        data.setParentId(organizationId);
        data.getChannels().addAll(createChannelSettings(data));

        assertThatThrownBy(() -> service.add(organizationId, data, ownerId)).isInstanceOf(DuplicateDataException.class)
                .hasFieldOrPropertyWithValue("entityName",
                        "NotificationSettings");
    }

    @Test
    @DisplayName("Редактирование")
    void test_edit() {
        var organizationId = UUID.randomUUID();

        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification.getChannels().addAll(createChannelSettings(notification));
        repository.save(notification);

        var newNotification = new NotificationSettings();
        newNotification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        newNotification.setType(NotificationType.ALLOCATION);
        newNotification.setName("New Name");
        newNotification.setDescription("New Description");
        newNotification.setId(notification.getId());
        newNotification.setParentId(organizationId);
        newNotification.getChannels().addAll(createChannelSettings(newNotification, "Prefix"));

        service.edit(organizationId, notification.getId(), newNotification);

        assertThat(notification.getName()).isEqualTo(newNotification.getName());
        assertThat(notification.getDescription()).isEqualTo(newNotification.getDescription());
        assertThat(notification.getType()).isEqualTo(newNotification.getType());
        assertThat(notification.getChannels()).hasSameSizeAs(newNotification.getChannels());
        assertThat(notification.getChannels().getFirst().getText())
                .isEqualTo(newNotification.getChannels().getFirst().getText());
        assertThat(notification.getChannels().getFirst().getChannel())
                .isEqualTo(newNotification.getChannels().getFirst().getChannel());
        assertThat(notification.getChannels().get(1).getText())
                .isEqualTo(newNotification.getChannels().get(1).getText());
        assertThat(notification.getChannels().get(1).getChannel())
                .isEqualTo(newNotification.getChannels().get(1).getChannel());
        assertThat(notification.getChannels().get(2).getText())
                .isEqualTo(newNotification.getChannels().get(2).getText());
        assertThat(notification.getChannels().get(2).getChannel())
                .isEqualTo(newNotification.getChannels().get(2).getChannel());
    }

    @Test
    @DisplayName("Редактирование на дубликат")
    void test_edit_duplicate() {
        var organizationId = UUID.randomUUID();

        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification.getChannels().addAll(createChannelSettings(notification));
        repository.save(notification);

        var value = new NotificationSettings();
        value.setName("New");
        value.setDescription("NEW Description");
        value.setNotificationClass(NotificationClass.LIMIT_PERSON);
        value.setType(NotificationType.ALLOCATION);
        value.setParentId(organizationId);
        repository.save(value);

        var newNotification = new NotificationSettings();
        newNotification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        newNotification.setType(NotificationType.ALLOCATION);
        newNotification.setName("New");
        newNotification.setDescription("NEW Description");
        newNotification.setParentId(organizationId);
        newNotification.setId(notification.getId());
        newNotification.getChannels().addAll(createChannelSettings(newNotification, "Prefix"));

        try {
            service.edit(organizationId, notification.getId(), newNotification);
        } catch (DuplicateDataException e) {
            assertThat(e)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings");
        }
    }

    @Test
    @DisplayName("Редактирование несуществующего")
    void test_edit_unExists() {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var newNotification = new NotificationSettings();
        newNotification.setNotificationClass(NotificationClass.USER_DELEGATE);
        newNotification.setType(NotificationType.ASSIGNMENT);
        newNotification.setName("New");
        newNotification.setDescription("New Description");
        newNotification.setId(id);
        newNotification.getChannels().addAll(createChannelSettings(newNotification, "Prefix"));

        assertThatThrownBy(() -> service.edit(organizationId, id, newNotification))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                .hasFieldOrPropertyWithValue("entityId", id);
    }

    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var organizationId = UUID.randomUUID();
        organizationRepository.save(Organization.builder().id(organizationId).build());

        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification.getChannels().addAll(createChannelSettings(notification));
        repository.save(notification);

        assertThat(repository.count()).isEqualTo(1);

        service.delete(organizationId, notification.getId());

        assertThat(repository.count()).isZero();

        verify(notificationService).cancelAll(any(NotificationSettings.class));
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void test_delete_unExists() {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        assertThatThrownBy(() -> service.delete(organizationId, id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "Organization")
                .hasFieldOrPropertyWithValue("entityId", organizationId);
    }

    @Test
    @DisplayName("Получение")
    void test_get() {
        var organizationId = UUID.randomUUID();

        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification.getChannels().addAll(createChannelSettings(notification));
        repository.save(notification);

        var saved = service.get(organizationId, notification.getId());

        assertThat(notification.getName()).isEqualTo(saved.getName());
        assertThat(notification.getDescription()).isEqualTo(saved.getDescription());
        assertThat(notification.getType()).isEqualTo(saved.getType());
        assertThat(notification.getChannels()).hasSameSizeAs(saved.getChannels());
        assertThat(notification.getChannels().getFirst().getText()).isEqualTo(saved.getChannels().getFirst().getText());
        assertThat(notification.getChannels().getFirst().getChannel()).isEqualTo(saved.getChannels().getFirst().getChannel());
        assertThat(notification.getChannels().get(1).getText()).isEqualTo(saved.getChannels().get(1).getText());
        assertThat(notification.getChannels().get(1).getChannel()).isEqualTo(saved.getChannels().get(1).getChannel());
        assertThat(notification.getChannels().get(2).getText()).isEqualTo(saved.getChannels().get(2).getText());
        assertThat(notification.getChannels().get(2).getChannel()).isEqualTo(saved.getChannels().get(2).getChannel());
    }

    @Test
    @DisplayName("Получение с типом уведомлений")
    void test_get_withType() {
        var organizationId = UUID.randomUUID();

        var notification = defaultNotificationSettingsService.getSettingByClassAndType(NotificationClass.REQUEST_TAXI,
                NotificationType.APPROVE);
        service.add(organizationId, notification, null);

        var saved = service.get(organizationId, NotificationClass.REQUEST_TAXI,
                NotificationType.APPROVE);

        assertThat(notification.getName()).isEqualTo(saved.getName());
        assertThat(notification.getDescription()).isEqualTo(saved.getDescription());
        assertThat(notification.getType()).isEqualTo(saved.getType());
        assertThat(notification.getChannels()).hasSameSizeAs(saved.getChannels());
        assertThat(notification.getChannels().getFirst().getText()).isEqualTo(saved.getChannels().getFirst().getText());
        assertThat(notification.getChannels().getFirst().getChannel()).isEqualTo(saved.getChannels().getFirst().getChannel());
        assertThat(notification.getChannels().get(1).getText()).isEqualTo(saved.getChannels().get(1).getText());
        assertThat(notification.getChannels().get(1).getChannel()).isEqualTo(saved.getChannels().get(1).getChannel());
        assertThat(notification.getChannels().get(2).getText()).isEqualTo(saved.getChannels().get(2).getText());
        assertThat(notification.getChannels().get(2).getChannel()).isEqualTo(saved.getChannels().get(2).getChannel());
    }

    @Test
    @DisplayName("Получение несуществующего")
    void test_get_unExists() {
        var id = UUID.randomUUID();

        try {
            service.get(UUID.randomUUID(), id);
        } catch (EntityNotFoundException e) {
            assertThat(e)
                    .hasFieldOrPropertyWithValue("entityName", "NotificationSettings")
                    .hasFieldOrPropertyWithValue("entityId", id);
        }
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var organizationId = UUID.randomUUID();
        organizationRepository.save(Organization.builder().id(organizationId).build());

        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification.getChannels().addAll(createChannelSettings(notification));
        repository.save(notification);

        var saved = service.getAll(organizationId);

        assertThat(notification.getName()).isEqualTo(saved.getFirst().getName());
        assertThat(notification.getDescription()).isEqualTo(saved.getFirst().getDescription());
        assertThat(notification.getType()).isEqualTo(saved.getFirst().getType());
        assertThat(notification.getChannels()).hasSameSizeAs(saved.getFirst().getChannels());
        assertThat(notification.getChannels().getFirst().getText()).isEqualTo(saved.getFirst().getChannels().getFirst().getText());
        assertThat(notification.getChannels().getFirst().getChannel())
                .isEqualTo(saved.getFirst().getChannels().getFirst().getChannel());
        assertThat(notification.getChannels().get(1).getText()).isEqualTo(saved.getFirst().getChannels().get(1).getText());
        assertThat(notification.getChannels().get(1).getChannel())
                .isEqualTo(saved.getFirst().getChannels().get(1).getChannel());
        assertThat(notification.getChannels().get(2).getText()).isEqualTo(saved.getFirst().getChannels().get(2).getText());
        assertThat(notification.getChannels().get(2).getChannel())
                .isEqualTo(saved.getFirst().getChannels().get(2).getChannel());
    }

    private List<ChannelSettings> createChannelSettings(NotificationSettings settings) {
        return createChannelSettings(settings, "");
    }

    private List<ChannelSettings> createChannelSettings(NotificationSettings settings, String prefix) {
        var data = new ArrayList<ChannelSettings>();
        for (var i = 0; i < 3; i++) {
            var channel = new ChannelSettings();
            channel.setChannel(ChannelType.values()[i]);
            channel.setText(prefix + "Text" + i);
            channel.setNotification(settings);

            data.add(channel);
        }
        return data;
    }

}