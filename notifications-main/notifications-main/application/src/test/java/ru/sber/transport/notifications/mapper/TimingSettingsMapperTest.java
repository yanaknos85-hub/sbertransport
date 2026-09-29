package ru.sber.transport.notifications.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.dto.timing.EventType;
import ru.sber.transport.notifications.dto.timing.TimingDto;
import ru.sber.transport.notifications.mapper.settings.TimingSettingsMapper;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("WrongUsageOfMappersFactory")
@DisplayName("Проверка маппера настроек тайминга")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class TimingSettingsMapperTest {
    
    private final TimingSettingsMapper mapper = Mappers.getMapper(TimingSettingsMapper.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    @DisplayName("Преобразование к модели")
    void test_toModel() {
        var dto = new HashMap<String, Object>();
        dto.put("timeBefore", 30 * 1000);
        dto.put("eventType", EventType.AT_EVENT);
        
        var expected = objectMapper.convertValue(dto, TimingDto.class);
        
        var actual = mapper.toModel(expected);
        
        assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
        assertThat(actual.getType()).isEqualTo(expected.getEventType().getModel());
    }
    
    @Test
    @DisplayName("Преобразование к модели. Нет типа")
    void test_toModel_noType() {
        var dto = new HashMap<String, Object>();
        dto.put("timeBefore", 30 * 1000);
        
        var expected = objectMapper.convertValue(dto, TimingDto.class);
        
        var actual = mapper.toModel(expected);
        
        assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
        assertThat(actual.getType()).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к модели. Нет объекта")
    void test_toModel_null() {
        var actual = mapper.toModel((TimingDto) null);
        
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к модели. Список")
    void test_toModel_list() {
        var dtos = new ArrayList<Map<String, Object>>();
        for (var i = 0; i < 10; i++) {
            var dto = new HashMap<String, Object>();
            dto.put("timeBefore", (30 + i) * 1000);
            dto.put("eventType", EventType.values()[i % EventType.values().length]);
            dtos.add(dto);
        }
    
        var expectedList = objectMapper.convertValue(dtos, new TypeReference<List<TimingDto>>() {
        });
    
        var actualList = mapper.toModel(expectedList);
    
        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = expectedList.get(i);
        
            assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
            assertThat(actual.getType()).isEqualTo(expected.getEventType().getModel());
        }
    }
    
    @Test
    @DisplayName("Преобразование к модели. Список. Нет объекта")
    void test_toModel_list_null() {
        var actualList = mapper.toModel((List<? extends TimingDto>) null);
    
        assertThat(actualList).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к передаче")
    void test_toDto() {
        var expected = new TimingSettings();
        expected.setId(UUID.randomUUID());
        expected.setTimeBefore(Duration.ofHours(1));
        expected.setType(EventType.AT_EVENT.getModel());
        
        var actual = mapper.toDto(expected);
        
        assertThat(actual.getEventType().name()).isEqualTo(expected.getType().name());
        assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
    }
    
    @Test
    @DisplayName("Преобразование к передаче. Нет типа")
    void test_toDto_noType() {
        var expected = new TimingSettings();
        expected.setId(UUID.randomUUID());
        expected.setTimeBefore(Duration.ofHours(1));
        
        var actual = mapper.toDto(expected);
        
        assertThat(actual.getEventType()).isNull();
        assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
    }
    
    @Test
    @DisplayName("Преобразование к передаче. Нет объекта")
    void test_toDto_null() {
        var actual = mapper.toDto((TimingSettings) null);
        
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к передаче списка")
    void test_toDto_list() {
        var expectedList = new ArrayList<TimingSettings>();
        for (var i = 0; i < 10; i++) {
            var expected = new TimingSettings();
            expected.setId(UUID.randomUUID());
            expected.setTimeBefore(Duration.ofHours(1));
            expected.setType(EventType.AT_EVENT.getModel());
            expectedList.add(expected);
        }
        
        var actualList = mapper.toDto(expectedList);
        
        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = expectedList.get(i);
            
            assertThat(actual.getEventType().name()).isEqualTo(expected.getType().name());
            assertThat(actual.getTimeBefore()).isEqualTo(expected.getTimeBefore());
        }
    }
    
    @Test
    @DisplayName("Преобразование к передаче списка. Нет объекта")
    void test_toDto_list_null() {
        var actualList = mapper.toDto((List<? extends TimingSettings>) null);
        
        assertThat(actualList).isNull();
    }
}