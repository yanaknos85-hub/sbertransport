package ru.sber.transport.notifications.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.notifications.dto.counting.CountingDto;
import ru.sber.transport.notifications.mapper.settings.CountSettingsMapper;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("WrongUsageOfMappersFactory")
@DisplayName("Проверка маппера настроек количественного триггера")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class CountSettingsMapperTest {
    
    private final CountSettingsMapper mapper = Mappers.getMapper(CountSettingsMapper.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    @DisplayName("Преобразование к модели одного")
    void test_toModel() {
        var dto = new HashMap<String, Object>();
        dto.put("value", 10);
        dto.put("type", CountType.EXACT);
        dto.put("property", "property");
    
        var expected = objectMapper.convertValue(dto, CountingDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getCount()).isEqualTo(expected.getValue());
        assertThat(actual.getType()).isEqualTo(expected.getType().getModel());
        assertThat(actual.getPropertyName()).isEqualTo(expected.getProperty());
    }
    
    @Test
    @DisplayName("Преобразование к модели одного. Нет типа")
    void test_toModel_noType() {
        var dto = new HashMap<String, Object>();
        dto.put("value", 10);
        dto.put("property", "property");
    
        var expected = objectMapper.convertValue(dto, CountingDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getCount()).isEqualTo(expected.getValue());
        assertThat(actual.getType()).isNull();
        assertThat(actual.getPropertyName()).isEqualTo(expected.getProperty());
    }
    
    @Test
    @DisplayName("Преобразование к модели одного. Нет объекта")
    void test_toModel_null() {
        var actual = mapper.toModel((CountingDto) null);
    
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к модели списка")
    void test_toModel_list() {
        var dtos = new ArrayList<Map<String, Object>>();
        for (var i = 0; i < 10; i++) {
            var dto = new HashMap<String, Object>();
            dto.put("value", 10);
            dto.put("type", CountType.values()[i % CountingType.values().length]);
            dto.put("property", "property");
            
            dtos.add(dto);
        }
    
        var expectedList = objectMapper.convertValue(dtos, new TypeReference<List<CountingDto>>() {});
    
        var actualList = mapper.toModel(expectedList);
    
        assertThat(actualList).hasSameSizeAs(expectedList);
        
        var i = 0;
        for (var actual : actualList) {
            var expected = expectedList.get(i++);
            
            assertThat(actual.getCount()).isEqualTo(expected.getValue());
            assertThat(actual.getType()).isEqualTo(expected.getType().getModel());
            assertThat(actual.getPropertyName()).isEqualTo(expected.getProperty());
        }
    }
    
    @Test
    @DisplayName("Преобразование к модели списка. Нет объекта")
    void test_toModel_list_null() {
        var actualList = mapper.toModel((List<? extends CountingDto>) null);
    
        assertThat(actualList).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к передаче одного")
    void test_toDto() {
        var counting = new CountingSettings();
        counting.setType(CountingType.EXACT);
        counting.setPropertyName("name");
        counting.setCount(10D);
    
        var actual = mapper.toDto(counting);
    
        assertThat(actual.getValue()).isEqualTo(counting.getCount());
        assertThat(actual.getType().getModel()).isEqualTo(counting.getType());
        assertThat(actual.getProperty()).isEqualTo(counting.getPropertyName());
    }
    
    @Test
    @DisplayName("Преобразование к передаче одного. Нет типа")
    void test_toDto_noType() {
        var counting = new CountingSettings();
        counting.setPropertyName("name");
        counting.setCount(10D);
    
        var actual = mapper.toDto(counting);
    
        assertThat(actual.getValue()).isEqualTo(counting.getCount());
        assertThat(actual.getType()).isNull();
        assertThat(actual.getProperty()).isEqualTo(counting.getPropertyName());
    }
    
    @Test
    @DisplayName("Преобразование к передаче одного. Нет объекта")
    void test_toDto_null() {
        var actual = mapper.toDto((CountingSettings) null);
    
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Преобразование к передаче списка")
    void test_toDto_list() {
        var settings = new ArrayList<CountingSettings>();
        for (var i = 0; i < 10; i++) {
            var dto = new CountingSettings();
            dto.setCount((double) i);
            dto.setPropertyName("property " + i);
            dto.setType(CountingType.values()[i % CountingType.values().length]);
            
            settings.add(dto);
        }
    
        var actualList = mapper.toDto(settings);
    
        assertThat(actualList).hasSameSizeAs(settings);
        
        var i = 0;
        for (var actual : actualList) {
            var expected = settings.get(i++);
    
            assertThat(actual.getValue()).isEqualTo(expected.getCount());
            assertThat(actual.getType().getModel()).isEqualTo(expected.getType());
            assertThat(actual.getProperty()).isEqualTo(expected.getPropertyName());
        }
    }
    
    @Test
    @DisplayName("Преобразование к передаче списка. Нет объекта")
    void test_toDto_list_null() {
        var actualList = mapper.toDto((List<? extends CountingSettings>) null);
    
        assertThat(actualList).isNull();
    }
}