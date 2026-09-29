package ru.sberbank.ditsib.geo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.config.properties.FormatProperties;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.config.properties.MappingFields;
import ru.sberbank.ditsib.geo.config.properties.MappingProperties;
import ru.sberbank.ditsib.geo.config.properties.region.RegionResolverProperties;
import ru.sberbank.ditsib.geo.service.RegionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка сервиса регионов")
class RegionServiceImplTest {

    private final GeoServiceClient client = mock(GeoServiceClient.class);

    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    private final GeoProperties properties = new GeoProperties();

    private final RegionService service = new RegionServiceImpl(client, new MapUtils(new ScriptUtils()), properties, mapper);

    @Test
    @DisplayName("Получение региона")
    void test_get_region() {
        var region = Instancio.create(String.class);

        var fields = new MappingFields();
        fields.put("id", "region.id");
        fields.put("name", "region.name");

        var response = new MappingProperties();
        response.setFields(fields);

        var format = new FormatProperties();
        format.setResponse(response);

        var regionProperties = new RegionResolverProperties();
        regionProperties.setFormat(format);
        properties.setRegion(regionProperties);

        when(client.getRegion(region)).thenReturn(Map.of("region", Map.of("id", region, "name", "region name")));

        var actual = service.get(region);

        assertThat(actual).isInstanceOf(Map.class)
            .containsEntry("id", region)
            .containsEntry("name", "region name")
        ;
    }

}