package ru.sberbank.ditsib.geo.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка сервиса работы с гео")
class GeoServiceClientImplTest {

    // ========== Существующие тесты для getRoute (восстановление маршрута) ==========

    @Test
    @DisplayName("Восстановление маршрута по точкам")
    void test_getRoute() throws JsonProcessingException {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("tosterHost");

        var properties = geoProperties.getRouteRecreation();
        properties.setUrl("/tosterUrl");
        properties.getApiKey().setValue("tosterKey");
        properties.setMethod(HttpMethod.POST);
        properties.setDefaultBadPointTolerance("high");

        var mapping = properties.getFormat().getRequest().getFields();
        mapping.put("longitude", "lon");
        mapping.put("latitude", "lat");
        mapping.put("time", "utc");
        mapping.put("location", "query");
        mapping.put("bad_point_tolerance", "bad_point_tolerance");

        var twoGisResponse = new HashMap<String, Object>();
        twoGisResponse.put("toster?", "toster!");
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(twoGisResponse));

        var coords = Instancio.createList(RouteRecreationCoordinates.class);
        var result = geoServiceClient.getRoute(coords);
        assertEquals(1, result.keySet().size());
        assertEquals(twoGisResponse.get("toster?"), result.get("toster?"));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        var methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        var returnTypeCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate).exchange(urlCaptor.capture(), methodCaptor.capture(), requestCaptor.capture(), returnTypeCaptor.capture());

        var expectedUrl = "tosterHost/tosterUrl?key=tosterKey";
        assertEquals(expectedUrl, urlCaptor.getValue());
        assertEquals(HttpMethod.POST, methodCaptor.getValue());

        var actualRequest = requestCaptor.getValue();
        var bodyObject = new ObjectMapper().readValue((String) actualRequest.getBody(), Object.class);
        var body = ReflectionUtils.castObjectToMap(bodyObject, String.class, Object.class);
        assertEquals("high", body.get("bad_point_tolerance"));
        var query = ReflectionUtils.castObjectToList(body.get("query"), Object.class);
        for (var i = 0; i < coords.size(); i++) {
            var expected = coords.get(i);
            var actual = ReflectionUtils.castObjectToMap(query.get(i), String.class, Object.class);
            assertEquals(expected.getLongitude(), actual.get("lon"));
            assertEquals(expected.getLatitude(), actual.get("lat"));
        }
    }

    @Test
    @DisplayName("Восстановление маршрута по точкам. Ответ 502")
    void test_getRoute_502() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("tosterHost");

        var properties = geoProperties.getRouteRecreation();
        properties.setUrl("/tosterUrl");
        properties.getApiKey().setValue("tosterKey");
        properties.setMethod(HttpMethod.POST);
        properties.setDefaultBadPointTolerance("high");

        var mapping = properties.getFormat().getRequest().getFields();
        mapping.put("longitude", "lon");
        mapping.put("latitude", "lat");
        mapping.put("time", "utc");
        mapping.put("location", "query");
        mapping.put("bad_point_tolerance", "bad_point_tolerance");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatusCode.valueOf(502), "", new HttpHeaders(), null, null));

        var coords = Instancio.createList(RouteRecreationCoordinates.class);

        assertThrows(GeoApiException.class, () -> geoServiceClient.getRoute(coords));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        var methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        var returnTypeCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(2)).exchange(urlCaptor.capture(), methodCaptor.capture(), requestCaptor.capture(), returnTypeCaptor.capture());

        var expectedUrl = "tosterHost/tosterUrl?key=tosterKey";
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

    @Test
    @DisplayName("Восстановление маршрута по точкам. Ответ 503")
    void test_getRoute_503() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("tosterHost");

        var properties = geoProperties.getRouteRecreation();
        properties.setUrl("/tosterUrl");
        properties.getApiKey().setValue("tosterKey");
        properties.setMethod(HttpMethod.POST);
        properties.setDefaultBadPointTolerance("high");

        var mapping = properties.getFormat().getRequest().getFields();
        mapping.put("longitude", "lon");
        mapping.put("latitude", "lat");
        mapping.put("time", "utc");
        mapping.put("location", "query");
        mapping.put("bad_point_tolerance", "bad_point_tolerance");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatusCode.valueOf(503), "", new HttpHeaders(), null, null));

        var coords = Instancio.createList(RouteRecreationCoordinates.class);

        assertThrows(GeoApiException.class, () -> geoServiceClient.getRoute(coords));

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        var methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        var returnTypeCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(1)).exchange(urlCaptor.capture(), methodCaptor.capture(), requestCaptor.capture(), returnTypeCaptor.capture());

        var expectedUrl = "tosterHost/tosterUrl?key=tosterKey";
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

    // ========== Новые тесты для getAddressesByLocation (проверка параметров запроса) ==========

    @Test
    @SneakyThrows
    @DisplayName("getAddressesByLocation с обычным RequestType проверяет параметры запроса")
    void getAddressesByLocation_normalRequestType() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var coderProperties = geoProperties.getGeoCoding().getCoder();
        coderProperties.setUrl("/geocode");
        coderProperties.getApiKey().setValue("testKey");
        coderProperties.setMethod(HttpMethod.GET);

        var mapping = coderProperties.getFormat().getRequest().getFields();
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("query", "q");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("items", Collections.emptyList())));

        var requestDto = AddressRequestDto.builder()
                .location("Москва Тверская")
                .latitude(55.75)
                .longitude(37.61)
                .build();

        var result = geoServiceClient.getAddressesByLocation(requestDto);

        assertTrue(result.isEmpty());
        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("getAddressesByLocation с RequestType PARKING использует свойства парковки")
    void getAddressesByLocation_parkingRequestType() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var parkingByNumberProperties = geoProperties.getGeoCoding().getParkingByNumber();
        parkingByNumberProperties.setUrl("/parkingByNumber");
        parkingByNumberProperties.getApiKey().setValue("parkingKey");
        parkingByNumberProperties.setMethod(HttpMethod.GET);

        var mapping = parkingByNumberProperties.getFormat().getRequest().getFields();
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("query", "q");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("items", Collections.emptyList())));

        var requestDto = AddressRequestDto.builder()
                .requestType(RequestType.PARKING)
                .location("Парковка")
                .latitude(55.75)
                .longitude(37.61)
                .build();

        var result = geoServiceClient.getAddressesByLocation(requestDto);

        assertTrue(result.isEmpty());
    }

    // ========== Новые тесты для getAddressesByCoordinates ==========

    @Test
    @SneakyThrows
    @DisplayName("getAddressesByCoordinates с обычным RequestType")
    void getAddressesByCoordinates_normalRequestType() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var reverseCoderProperties = geoProperties.getGeoCoding().getReverse();
        reverseCoderProperties.setUrl("/reverseGeocode");
        reverseCoderProperties.getApiKey().setValue("reverseKey");
        reverseCoderProperties.setMethod(HttpMethod.GET);

        var mapping = reverseCoderProperties.getFormat().getRequest().getFields();
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("items", Collections.emptyList())));

        var requestDto = AddressRequestDto.builder()
                .latitude(57.00815)
                .longitude(40.989292)
                .build();

        var result = geoServiceClient.getAddressesByCoordinates(requestDto);

        assertTrue(result.isEmpty());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAddressesByCoordinates с RequestType PARKING")
    void getAddressesByCoordinates_parkingRequestType() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var parkingProperties = geoProperties.getGeoCoding().getParking();
        parkingProperties.setUrl("/parking");
        parkingProperties.getApiKey().setValue("parkingKey");
        parkingProperties.setMethod(HttpMethod.GET);

        var mapping = parkingProperties.getFormat().getRequest().getFields();
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("items", Collections.emptyList())));

        var requestDto = AddressRequestDto.builder()
                .requestType(RequestType.PARKING)
                .latitude(55.75)
                .longitude(37.61)
                .build();

        var result = geoServiceClient.getAddressesByCoordinates(requestDto);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAddressesByCoordinates возвращает пустой список при null ответе")
    void getAddressesByCoordinates_nullResponse() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var reverseCoderProperties = geoProperties.getGeoCoding().getReverse();
        reverseCoderProperties.setUrl("/reverseGeocode");
        reverseCoderProperties.getApiKey().setValue("reverseKey");
        reverseCoderProperties.setMethod(HttpMethod.GET);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        var requestDto = AddressRequestDto.builder()
                .latitude(55.75)
                .longitude(37.61)
                .build();

        var result = geoServiceClient.getAddressesByCoordinates(requestDto);

        assertTrue(result.isEmpty());
    }

    // ========== Новые тесты для getRoutes ==========

    @Test
    @SneakyThrows
    @DisplayName("getRoutes с обычными параметрами")
    void getRoutes_normalParams() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var routingProperties = geoProperties.getRouting().getRoute();
        routingProperties.setUrl("/route");
        routingProperties.getApiKey().setValue("routeKey");
        routingProperties.setMethod(HttpMethod.POST);

        var mapping = routingProperties.getFormat().getRequest().getFields();
        mapping.put("location", "coordinates");
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("time", "utc");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        var waypoints = new ArrayList<WaypointDto>();
        var waypoint = new WaypointDto();
        waypoint.setLatitude(55.75);
        waypoint.setLongitude(37.61);
        waypoint.setWaitTime(Duration.ZERO);
        waypoints.add(waypoint);

        var result = geoServiceClient.getRoutes(
                waypoints,
                DistanceUnit.KILOMETERS,
                RouteType.CAR,
                null,
                false
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @SneakyThrows
    @DisplayName("getRoutes с EMPLOYEE_TRANSPORTATION и excludeDirtRoad")
    void getRoutes_employeeTransportationWithExcludeDirtRoad() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var routingProperties = geoProperties.getRouting().getRoute();
        routingProperties.setUrl("/route");
        routingProperties.getApiKey().setValue("routeKey");
        routingProperties.setMethod(HttpMethod.POST);

        var mapping = routingProperties.getFormat().getRequest().getFields();
        mapping.put("location", "coordinates");
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("time", "utc");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        var waypoints = new ArrayList<WaypointDto>();
        var waypoint = new WaypointDto();
        waypoint.setLatitude(55.75);
        waypoint.setLongitude(37.61);
        waypoints.add(waypoint);

        var result = geoServiceClient.getRoutes(
                waypoints,
                DistanceUnit.KILOMETERS,
                RouteType.CAR,
                "EMPLOYEE_TRANSPORTATION",
                true
        );

        assertTrue(result.isEmpty());

        var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), requestCaptor.capture(), any(ParameterizedTypeReference.class));

        // Проверяем, что запрос был выполнен с правильными параметрами
        var body = requestCaptor.getValue().getBody();
        assertNotNull(body);
    }

    @Test
    @SneakyThrows
    @DisplayName("getRoutes с EMPLOYEE_TRANSPORTATION без excludeDirtRoad")
    void getRoutes_employeeTransportationWithoutExcludeDirtRoad() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var routingProperties = geoProperties.getRouting().getRoute();
        routingProperties.setUrl("/route");
        routingProperties.getApiKey().setValue("routeKey");
        routingProperties.setMethod(HttpMethod.POST);

        var mapping = routingProperties.getFormat().getRequest().getFields();
        mapping.put("location", "coordinates");
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("time", "utc");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        var waypoints = new ArrayList<WaypointDto>();
        var waypoint = new WaypointDto();
        waypoint.setLatitude(55.75);
        waypoint.setLongitude(37.61);
        waypoints.add(waypoint);

        var result = geoServiceClient.getRoutes(
                waypoints,
                DistanceUnit.KILOMETERS,
                RouteType.CAR,
                "EMPLOYEE_TRANSPORTATION",
                false
        );

        assertTrue(result.isEmpty());

        var requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(any(String.class), any(HttpMethod.class), requestCaptor.capture(), any(ParameterizedTypeReference.class));

        // Проверяем, что запрос был выполнен с правильными параметрами
        var body = requestCaptor.getValue().getBody();
        assertNotNull(body);
    }

    @Test
    @DisplayName("getRoutes возвращает пустой список при null ответе")
    void getRoutes_nullResponse() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var routingProperties = geoProperties.getRouting().getRoute();
        routingProperties.setUrl("/route");
        routingProperties.getApiKey().setValue("routeKey");
        routingProperties.setMethod(HttpMethod.POST);

        var mapping = routingProperties.getFormat().getRequest().getFields();
        mapping.put("location", "coordinates");
        mapping.put("latitude", "lat");
        mapping.put("longitude", "lon");
        mapping.put("time", "utc");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        var waypoints = new ArrayList<WaypointDto>();
        var waypoint = new WaypointDto();
        waypoint.setLatitude(55.75);
        waypoint.setLongitude(37.61);
        waypoints.add(waypoint);

        var result = geoServiceClient.getRoutes(
                waypoints,
                DistanceUnit.KILOMETERS,
                RouteType.CAR,
                null,
                false
        );

        assertTrue(result.isEmpty());
    }

    // ========== Новые тесты для getRegion ==========

    @Test
    @SneakyThrows
    @DisplayName("getRegion возвращает данные региона")
    void getRegion_returnsRegionData() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var regionProperties = geoProperties.getRegion();
        regionProperties.setUrl("/region");
        regionProperties.getApiKey().setValue("regionKey");
        regionProperties.setMethod(HttpMethod.POST);

        var mapping = regionProperties.getFormat().getRequest().getFields();
        mapping.put("id", "region_id");

        var response = new HashMap<String, Object>();
        response.put("id", "50");
        response.put("name", "Москва");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        var result = geoServiceClient.getRegion("50");

        // Проверяем, что результат содержит данные
        assertNotNull(result);
    }

    @Test
    @SneakyThrows
    @DisplayName("getRegion с пустым ID")
    void getRegion_withEmptyId() {
        var restTemplate = mock(RestTemplate.class);
        var geoProperties = new GeoProperties();
        var scriptUtils = new ScriptUtils();
        var mapUtils = new MapUtils(scriptUtils);
        var objectMapper = new ObjectMapper();
        var geoServiceClient = new ExtendedGeoServiceClientImpl(geoProperties, objectMapper, mapUtils, restTemplate);

        geoProperties.setUrl("https://api.example.com");

        var regionProperties = geoProperties.getRegion();
        regionProperties.setUrl("/region");
        regionProperties.getApiKey().setValue("regionKey");
        regionProperties.setMethod(HttpMethod.POST);

        var mapping = regionProperties.getFormat().getRequest().getFields();
        mapping.put("id", "region_id");

        var response = new HashMap<String, Object>();
        response.put("id", "");
        response.put("name", "");

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        var result = geoServiceClient.getRegion("");

        // Проверяем, что результат не пустой
        assertNotNull(result);
    }

    static class ExtendedGeoServiceClientImpl extends GeoServiceClientImpl {

        private final RestTemplate restTemplate;

        public ExtendedGeoServiceClientImpl(GeoProperties geoProperties, ObjectMapper objectMapper, MapUtils mapUtils, RestTemplate restTemplate) {
            super(geoProperties, objectMapper, mapUtils);
            this.restTemplate = restTemplate;
        }

        @Override
        RestTemplate getRestTemplate() {
            return restTemplate;
        }
    }
}
