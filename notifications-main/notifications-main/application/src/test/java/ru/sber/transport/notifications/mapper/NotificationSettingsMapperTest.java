package ru.sber.transport.notifications.mapper;

import io.qameta.allure.Feature;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.mapper.settings.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.notifications.dto.notification.Channel;
import ru.sber.transport.notifications.dto.notification.NewNotificationSettingsDto;
import ru.sber.transport.notifications.dto.notification.NotificationClassDto;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка маппера настроек уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationSettingsMapperTest {

    private final RoleMapper roleMapper = new RoleMapperImpl();
    
    private final NotificationSettingsMapper mapper =
            new NotificationSettingsMapperImpl(new ChannelSettingsMapperImpl(), new CountSettingsMapperImpl(),
                                               new RestrictionSettingsMapperImpl(roleMapper), new TimingSettingsMapperImpl()
            );
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель")
    void test_notification_toModel() {
        var channel = new HashMap<String, Object>();
        channel.put("channel", Channel.EMAIL);
        channel.put("text", "Text");
        
        var dto = new HashMap<String, Object>();
        dto.put("notificationClass", NotificationClassDto.LIMIT_DEPARTMENT);
        dto.put("notificationType", NotificationType.ALLOCATION);
        dto.put("name", "Name");
        dto.put("description", "Description");
        dto.put("channels", List.of(channel));
    
        var expected = objectMapper.convertValue(dto, NewNotificationSettingsDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getType().name()).isEqualTo(expected.getNotificationType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).hasSameSizeAs(expected.getChannels());
        assertThat(actual.getChannels().get(0).getChannel())
                .isEqualTo(expected.getChannels().get(0).getChannel().getModel());
        assertThat(actual.getChannels().get(0).getText()).isEqualTo(expected.getChannels().get(0).getText());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель. Нет типа канала")
    void test_notification_toModel_noChannelType() {
        var channel = new HashMap<String, Object>();
        channel.put("text", "Text");
        
        var dto = new HashMap<String, Object>();
        dto.put("notificationClass", NotificationClassDto.LIMIT_DEPARTMENT);
        dto.put("notificationType", NotificationType.ALLOCATION);
        dto.put("name", "Name");
        dto.put("description", "Description");
        dto.put("channels", List.of(channel));
    
        var expected = objectMapper.convertValue(dto, NewNotificationSettingsDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getType().name()).isEqualTo(expected.getNotificationType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).hasSameSizeAs(expected.getChannels());
        assertThat(actual.getChannels().get(0).getChannel()).isNull();
        assertThat(actual.getChannels().get(0).getText()).isEqualTo(expected.getChannels().get(0).getText());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель. Нет канала")
    void test_notification_toModel_noChannel() {
        var dto = new HashMap<String, Object>();
        dto.put("notificationClass", NotificationClassDto.LIMIT_DEPARTMENT);
        dto.put("notificationType", NotificationType.ALLOCATION);
        dto.put("name", "Name");
        dto.put("description", "Description");
    
        var expected = objectMapper.convertValue(dto, NewNotificationSettingsDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getType().name()).isEqualTo(expected.getNotificationType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).isEmpty();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель. Нет объекта")
    void test_notification_toModel_null() {
        var actual = mapper.toModel((NewNotificationSettingsDto) null);
    
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Проверка преобразования объектов уведомлений с модели на клиент")
    void test_notification_toDto_list() {
        var count = 100;
        var settingsList = new ArrayList<NotificationSettings>();
        for (var i = 0; i < 100; i++) {
            var id = UUID.randomUUID();
    
            var settings = new ChannelSettings();
            settings.setChannel(ChannelType.values()[i % ChannelType.values().length]);
            settings.setText("text " + i);
    
            var expected = new NotificationSettings();
            expected.setId(id);
            expected.setNotificationClass(NotificationClass.values()[i % NotificationClassDto
                    .values().length]);
            expected.setType(NotificationType.values()[i % NotificationType
                    .values().length]);
            expected.setName("Name " + i);
            expected.setDescription("Description " + i);
            expected.getChannels().add(settings);
            expected.setId(UUID.randomUUID());
            
            settingsList.add(expected);
        }
    
        var actualList = mapper.toDto(settingsList);
    
        assertThat(actualList).hasSize(count);
        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);
            var expected = settingsList.get(i);
            
            assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getNotificationType().name()).isEqualTo(expected.getType().name());
            assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
            assertThat(actual.getChannels()).hasSameSizeAs(expected.getChannels());
            assertThat(actual.getChannels().get(0).getChannel().getModel())
                    .isEqualTo(expected.getChannels().get(0).getChannel());
            assertThat(actual.getChannels().get(0).getText()).isEqualTo(expected.getChannels().get(0).getText());
        }
    }
    
    @Test
    @DisplayName("Проверка преобразования объектов уведомлений с модели на клиент. Нет объекта")
    void test_notification_toDto_list_null() {
        var actualList = mapper.toDto((List<NotificationSettings>) null);
    
        assertThat(actualList).isNull();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с модели на клиент")
    void test_notification_toDto() {
        var id = UUID.randomUUID();
        
        var settings = new ChannelSettings();
        settings.setChannel(ChannelType.EMAIL);
        settings.setText("text");
        
        var expected = new NotificationSettings();
        expected.setId(id);
        expected.setNotificationClass(NotificationClass.LIMIT_PERSON);
        expected.setType(NotificationType.ALLOCATION);
        expected.setName("Name");
        expected.setDescription("Description");
        expected.getChannels().add(settings);
        expected.setId(UUID.randomUUID());
    
        var actual = mapper.toDto(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getNotificationType().name()).isEqualTo(expected.getType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).hasSameSizeAs(expected.getChannels());
        assertThat(actual.getChannels().get(0).getChannel().getModel())
                .isEqualTo(expected.getChannels().get(0).getChannel());
        assertThat(actual.getChannels().get(0).getText()).isEqualTo(expected.getChannels().get(0).getText());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с модели на клиент. Нет типа канала")
    void test_notification_toDto_nuChannelType() {
        var id = UUID.randomUUID();
        
        var settings = new ChannelSettings();
        settings.setText("text");
        
        var expected = new NotificationSettings();
        expected.setId(id);
        expected.setNotificationClass(NotificationClass.LIMIT_PERSON);
        expected.setType(NotificationType.ALLOCATION);
        expected.setName("Name");
        expected.setDescription("Description");
        expected.getChannels().add(settings);
        expected.setId(UUID.randomUUID());
    
        var actual = mapper.toDto(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getNotificationType().name()).isEqualTo(expected.getType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).hasSameSizeAs(expected.getChannels());
        assertThat(actual.getChannels().get(0).getChannel()).isNull();
        assertThat(actual.getChannels().get(0).getText()).isEqualTo(expected.getChannels().get(0).getText());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с модели на клиент. Нет канала")
    void test_notification_toDto_nuChannels() {
        var id = UUID.randomUUID();
        
        var expected = new NotificationSettings();
        expected.setId(id);
        expected.setNotificationClass(NotificationClass.LIMIT_PERSON);
        expected.setType(NotificationType.ALLOCATION);
        expected.setName("Name");
        expected.setDescription("Description");
        expected.setId(UUID.randomUUID());
    
        var actual = mapper.toDto(expected);
    
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getNotificationType().name()).isEqualTo(expected.getType().name());
        assertThat(actual.getNotificationClass().name()).isEqualTo(expected.getNotificationClass().name());
        assertThat(actual.getChannels()).isEmpty();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с модели на клиент. Нет объекта")
    void test_notification_toDto_null() {
        var actual = mapper.toDto((NotificationSettings) null);
    
        assertThat(actual).isNull();
    }
    
}