package ru.sber.transport.notifications.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.dto.notification.Channel;
import ru.sber.transport.notifications.dto.notification.ChannelSettingsDto;
import ru.sber.transport.notifications.mapper.settings.ChannelSettingsMapper;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("WrongUsageOfMappersFactory")
@DisplayName("Проверка маппера настроек канала уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class ChannelSettingsMapperTest {
    
    private final ChannelSettingsMapper mapper = Mappers.getMapper(ChannelSettingsMapper.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления с клиента в модель")
    void test_notificationSettings_toModel() {
        var dto = new HashMap<String, Object>();
        dto.put("channel", Channel.EMAIL);
        dto.put("text", "Text");
        
        var expected = objectMapper.convertValue(dto, ChannelSettingsDto.class);
        
        var actual = mapper.toModel(expected);
        
        assertThat(actual.getText()).isEqualTo(expected.getText());
        assertThat(actual.getChannel()).isEqualTo(expected.getChannel().getModel());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления с клиента в модель. Нет канала")
    void test_notificationSettings_toModel_noChannel() {
        var dto = new HashMap<String, Object>();
        dto.put("text", "Text");
        
        var expected = objectMapper.convertValue(dto, ChannelSettingsDto.class);
        
        var actual = mapper.toModel(expected);
        
        assertThat(actual.getText()).isEqualTo(expected.getText());
        assertThat(actual.getChannel()).isNull();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления с клиента в модель. Нет объекта")
    void test_notificationSettings_toModel_null() {
        var actual = mapper.toModel(null);
        
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления из модели в клиент")
    void test_notificationSettings_toDto() {
        var expected = new ChannelSettings();
        expected.setText("Text");
        expected.setChannel(ChannelType.PUSH);
        
        var actual = mapper.toDto(expected);
        
        assertThat(actual.getText()).isEqualTo(expected.getText());
        assertThat(actual.getChannel().getModel()).isEqualTo(expected.getChannel());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления из модели в клиент. Нет канала")
    void test_notificationSettings_toDto_channelType_null() {
        var expected = new ChannelSettings();
        expected.setText("Text");
        
        var actual = mapper.toDto(expected);
        
        assertThat(actual.getText()).isEqualTo(expected.getText());
        assertThat(actual.getChannel()).isNull();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта настройки уведомления из модели в клиент. Нет объекта")
    void test_notificationSettings_toDto_null() {
        var actual = mapper.toDto(null);
        
        assertThat(actual).isNull();
    }
    
}