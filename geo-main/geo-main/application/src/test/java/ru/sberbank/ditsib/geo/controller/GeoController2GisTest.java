package ru.sberbank.ditsib.geo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.dto.AddressDto;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.geo.controller.Constants.REPAIR;

@SuppressWarnings({"unchecked"})
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера адресов 2 gis")
@ActiveProfiles(value = {"2gis", "test"})
@EmbeddedPostgres
@MockBean(JwtDecoder.class)
class GeoController2GisTest {

    public static final int WAIT_TIME = 600000;

    private static final String USER_ROLE = "GUEST";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeoProperties properties;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() throws JsonProcessingException {
        AuthorizeUtils.authorize(roleCheckService);

        var reversedData =
                objectMapper.readValue(reversedSingleData(), new TypeReference<LinkedHashMap<String, Object>>() {
                });
        var data = objectMapper.readValue(singleData(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
        var regionData =
                objectMapper.readValue(regionData(), new TypeReference<LinkedHashMap<String, Object>>() {
                });
        when(restTemplate.exchange(contains("/suggests"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(data));
        when(restTemplate.exchange(contains("/geocode"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(reversedData));
        when(restTemplate.exchange(contains("/region"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(regionData));
    }

    @Test
    @DisplayName("Получение адреса")
    void getAddress() throws Exception {
        var response =
                mockMvc.perform(get("/address?latitude=57.00815&longitude=40.989292")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn();

        var actual = objectMapper
                .readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<List<AddressDto>>() {
                        });

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getStreet()).isEqualTo("Шереметевский проспект");
        assertThat(actual.get(0).getHouse()).isEqualTo("85Б к21 стр32");
        assertThat(actual.get(0).getCity()).isEmpty();
        assertThat(actual.get(0).getRegion()).isNull();
        assertThat(actual.get(0).getCountry()).isNull();
        assertThat(actual.get(1).getStreet()).isEqualTo("Шереметевский проспект");
        assertThat(actual.get(1).getHouse()).isEqualTo("85Б");
        assertThat(actual.get(1).getCity()).isEmpty();
        assertThat(actual.get(1).getRegion()).isNull();
        assertThat(actual.get(1).getCountry()).isNull();
    }

    @Test
    @DisplayName("Получение координат")
    void getCoordinates() throws Exception {
        var response =
                mockMvc.perform(get("/address?location=Иваново Демидова")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andReturn();

        var actual = objectMapper
                .readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<List<AddressDto>>() {
                        });

        assertThat(actual.get(0).getLatitude()).isEqualTo(57.005947);
        assertThat(actual.get(0).getLongitude()).isEqualTo(40.97225);

        mockMvc.perform(get("/address?location=Иваново Демидова2")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка получения маршрута в КМ")
    void getRoute_km() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        mockMvc.perform(post("/route").contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest("kilometers"))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$.distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$.time").value(34421000 + 18224000))
                .andExpect(jsonPath("$.segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$.segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$.segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$.segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$.segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$.segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$.segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$.segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$.segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$.segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$.segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$.segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$.segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$.segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    @DisplayName("Проверка получения маршрута в юнитах расстояния по-умолчанию")
    void getRoute() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        mockMvc.perform(post("/route")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest()))
                .andExpect(status().isOk())
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$.distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$.time").value(34421000 + 18224000))
                .andExpect(jsonPath("$.segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$.segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$.segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$.segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$.segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$.segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$.segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$.segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$.segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$.segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$.segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$.segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$.segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$.segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    @DisplayName("Проверка получения маршрута в милях")
    void getRoute_m() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeMilesData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        ResultActions resultActions = mockMvc.perform(post("/route")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest("miles")))
                .andExpect(status().isOk());
        resultActions
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$.distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$.time").value(34421000 + 18224000))
                .andExpect(jsonPath("$.segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$.segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$.segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$.segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$.segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$.segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$.segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$.segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$.segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$.segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$.segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$.segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$.segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$.segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    @DisplayName("Проверка получения маршрутов в КМ")
    void getRoutes_km() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        mockMvc.perform(post("/routes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest("kilometers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$[0].distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$[0].time").value(34421000 + 18224000))
                .andExpect(jsonPath("$[0].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[0].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].longitude").value(30.309817))

                .andExpect(jsonPath("$[1].distance").value(694.048 + 343.615))
                .andExpect(jsonPath("$[1].time").value(34421000 + 18660000))
                .andExpect(jsonPath("$[1].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[1].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    @DisplayName("Проверка получения маршрутов в юнитах расстояния по-умолчанию")
    void getRoutes() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        mockMvc.perform(post("/routes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$[0].distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$[0].time").value(34421000 + 18224000))
                .andExpect(jsonPath("$[0].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[0].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].longitude").value(30.309817))

                .andExpect(jsonPath("$[1].distance").value(694.048 + 343.615))
                .andExpect(jsonPath("$[1].time").value(34421000 + 18660000))
                .andExpect(jsonPath("$[1].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[1].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    @DisplayName("Проверка получения маршрутов в юнитах расстояния по-умолчанию, transportServiceType=EMPLOYEE_TRANSPORTATION")
    void getRoutes_EMPLOYEE_TRANSPORTATION() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(0),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });
        mockMvc.perform(post("/routes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequestTwoPoint("kilometers", Constants.EMPLOYEE_TRANSPORTATION)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].distance").value(694.048))
                .andExpect(jsonPath("$[0].time").value(34421000))
                .andExpect(jsonPath("$[0].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[0].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].longitude").value(30.309817));

        var captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(contains(properties.getRouteProperties().getUrl()), any(HttpMethod.class), captor.capture(), any(ParameterizedTypeReference.class));
        var body = Objects.requireNonNull(captor.getValue().getBody()).toString();
        assertThat(body).contains("\"type\":\"statistic\"");
        assertThat(body).contains("\"filters\":[\"dirt_road\"]");
    }

    @Test
    @DisplayName("Проверка получения маршрутов в юнитах расстояния по-умолчанию, не удалось построить маршрут без грунтовых дорог transportServiceType=EMPLOYEE_TRANSPORTATION")
    void getRoutes_failed_EMPLOYEE_TRANSPORTATION() throws Exception {
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new GeoApiException(404, Collections.emptyList()))
                .then(inv -> {
            var routeData = objectMapper.readValue(routeKilometersData(0),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });
        mockMvc.perform(post("/routes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequestTwoPoint("kilometers", Constants.EMPLOYEE_TRANSPORTATION)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].distance").value(694.048))
                .andExpect(jsonPath("$[0].time").value(34421000))
                .andExpect(jsonPath("$[0].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[0].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].longitude").value(30.309817));

        var captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).exchange(contains(properties.getRouteProperties().getUrl()), any(HttpMethod.class), captor.capture(), any(ParameterizedTypeReference.class));
        var body = Objects.requireNonNull(captor.getValue().getBody()).toString();
        assertThat(body).contains("\"type\":\"statistic\"");
        assertThat(body).doesNotContain("\"filters\":[\"dirt_road\"]");
    }

    @Test
    @DisplayName("Проверка получения маршрутов в милях")
    void getRoutes_m() throws Exception {
        var index = new AtomicInteger();
        when(restTemplate.exchange(contains(properties.getRouteProperties().getUrl()),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).then(inv -> {
            var routeData = objectMapper.readValue(routeMilesData(index.getAndIncrement()),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            return ResponseEntity.ok(routeData);
        });

        ResultActions resultActions = mockMvc.perform(post("/routes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                        .content(createRouteRequest("miles")))
                .andExpect(status().isOk());
        resultActions
                .andExpect(jsonPath("$.length()").value(2))
                // There are 3 waypoints in the source JSON, that indicates 2 segments will be on the route. One
                // request for one segment.
                .andExpect(jsonPath("$[0].distance").value(694.048 + 299.465))
                .andExpect(jsonPath("$[0].time").value(34421000 + 18224000))
                .andExpect(jsonPath("$[0].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[0].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[0].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[0].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[0].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[0].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[0].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[0].segments[1].coordinates[25].longitude").value(30.309817))

                .andExpect(jsonPath("$[1].distance").value(694.048 + 343.615))
                .andExpect(jsonPath("$[1].time").value(34421000 + 18660000))
                .andExpect(jsonPath("$[1].segments[0].coordinates.length()").value(21))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].latitude").value(59.952750))
                .andExpect(jsonPath("$[1].segments[0].coordinates[0].longitude").value(30.314512))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].latitude").value(59.953096))
                .andExpect(jsonPath("$[1].segments[0].coordinates[10].longitude").value(30.320609))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].latitude").value(59.946343))
                .andExpect(jsonPath("$[1].segments[0].coordinates[20].longitude").value(30.329367))
                .andExpect(jsonPath("$[1].segments[1].coordinates.length()").value(44))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].latitude").value(59.945844))
                .andExpect(jsonPath("$[1].segments[1].coordinates[5].longitude").value(30.327173))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].latitude").value(59.942635))
                .andExpect(jsonPath("$[1].segments[1].coordinates[15].longitude").value(30.316743))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].latitude").value(59.940142))
                .andExpect(jsonPath("$[1].segments[1].coordinates[25].longitude").value(30.309817));
    }

    @Test
    void testSpecialCases() throws Exception {
        when(restTemplate.exchange(contains("/suggests"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(
                        objectMapper.readValue("{\"meta\":{\"api_version\":\"3.0.17577\",\"code\":404,\"error\":{\"message\":\"Results not found\",\"type\":\"itemNotFound\"},\"issue_date\":\"20231212\"}}", new TypeReference<Map<String, Object>>() {
                        })));
        when(restTemplate.exchange(contains("/items"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(
                objectMapper.readValue("{\"meta\":{\"api_version\":\"3.0.17577\",\"code\":200,\"issue_date\":\"20231212\"},\"result\":{\"items\":[{\"address\":{\"building_id\":\"7318985049638312\",\"building_name\":\"ТЦ Три Кита\",\"components\":[{\"number\":\"72\",\"street\":\"Пионерская улица\",\"street_id\":\"7319088128851986\",\"type\":\"street_number\"}],\"postcode\":\"675000\"},\"address_name\":\"Пионерская улица, 72\",\"adm_div\":[{\"id\":\"1\",\"name\":\"Россия\",\"type\":\"country\"},{\"id\":\"1267655302447142\",\"name\":\"Амурская область\",\"type\":\"region\"},{\"id\":\"70030076118166868\",\"name\":\"Благовещенск городской округ\",\"type\":\"district_area\"},{\"city_alias\":\"blagoveshensk\",\"flags\":{\"is_default\":true,\"is_district_area_center\":true,\"is_region_center\":true},\"id\":\"7318972164734977\",\"is_default\":true,\"name\":\"Благовещенск\",\"type\":\"city\"}],\"building_name\":\"Три Кита, торговый центр\",\"full_name\":\"Благовещенск, Три Кита, торговый центр\",\"id\":\"7318877675512764\",\"name\":\"Три кита, торговый центр\",\"point\":{\"lat\":50.270076,\"lon\":127.532665},\"purpose_name\":\"Торговый центр\",\"type\":\"branch\"}],\"total\":1}}", new TypeReference<Map<String, Object>>() {
                })));
        var response = mockMvc.perform(get("/address?location=Пионерская, 72/1, Благовещенск")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();
        var actual = objectMapper
                .readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<List<AddressDto>>() {
                        });
        assert !actual.isEmpty();
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(50.270076, actual.get(0).getLatitude());
        Assertions.assertEquals(127.532665, actual.get(0).getLongitude());

        when(restTemplate.exchange(contains("/suggests"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(
                objectMapper.readValue("{\"meta\":{\"api_version\":\"3.0.17577\",\"code\":200,\"issue_date\":\"20231212\"},\"result\":{\"items\":[{\"address\":{\"building_id\":\"4504235282747233\",\"components\":[{\"number\":\"25/9\",\"street\":\"Тверская улица\",\"street_id\":\"4504338361745952\",\"type\":\"street_number\"},{\"number\":\"9\",\"street\":\"Мамоновский переулок\",\"street_id\":\"4504338361748162\",\"type\":\"street_number\"}],\"postcode\":\"125009\"},\"address_name\":\"Тверская улица, 25/9 / Мамоновский переулок, 9\",\"adm_div\":[{\"id\":\"1\",\"name\":\"Россия\",\"type\":\"country\"},{\"id\":\"5349042514588558\",\"name\":\"Москва\",\"type\":\"region\"},{\"city_alias\":\"moscow\",\"flags\":{\"is_default\":true,\"is_region_center\":true},\"id\":\"4504222397630173\",\"is_default\":true,\"name\":\"Москва\",\"type\":\"city\"},{\"id\":\"4504209512726536\",\"name\":\"Тверской\",\"type\":\"district\"}],\"full_name\":\"Москва, Тверская улица, 25/9 / Мамоновский переулок, 9\",\"id\":\"4504235282747233\",\"name\":\"Тверская улица, 25/9 / Мамоновский переулок, 9\",\"point\":{\"lat\":55.767284,\"lon\":37.600175},\"purpose_name\":\"Жилой дом\",\"search_attributes\":{\"handling_type\":0,\"suggest_parts\":[{\"is_suggested\":false,\"text\":\"Москва, тверская, 25/9\"}],\"suggested_text\":\"Москва, тверская, 25/9\"},\"type\":\"building\"},{\"address\":{\"building_id\":\"4504235282747255\",\"components\":[{\"number\":\"25/9 ст3\",\"street\":\"Тверская улица\",\"street_id\":\"4504338361745952\",\"type\":\"street_number\"}],\"postcode\":\"125009\"},\"address_name\":\"Тверская улица, 25/9 ст3\",\"adm_div\":[{\"id\":\"1\",\"name\":\"Россия\",\"type\":\"country\"},{\"id\":\"5349042514588558\",\"name\":\"Москва\",\"type\":\"region\"},{\"city_alias\":\"moscow\",\"flags\":{\"is_default\":true,\"is_region_center\":true},\"id\":\"4504222397630173\",\"is_default\":true,\"name\":\"Москва\",\"type\":\"city\"},{\"id\":\"4504209512726536\",\"name\":\"Тверской\",\"type\":\"district\"}],\"full_name\":\"Москва, Тверская улица, 25/9 ст3\",\"id\":\"4504235282747255\",\"name\":\"Тверская улица, 25/9 ст3\",\"point\":{\"lat\":55.767191,\"lon\":37.5997},\"purpose_name\":\"Гараж\",\"search_attributes\":{\"handling_type\":0,\"suggest_parts\":[{\"is_suggested\":false,\"text\":\"Москва, тверская, 25/9\"},{\"is_suggested\":true,\"text\":\" ст3\"}],\"suggested_text\":\"Москва, тверская, 25/9 ст3\"},\"type\":\"building\"},{\"address\":{\"building_id\":\"70030076128621596\",\"components\":[{\"number\":\"25/9 ст4\",\"street\":\"Тверская улица\",\"street_id\":\"4504338361745952\",\"type\":\"street_number\"}],\"postcode\":\"125009\"},\"address_name\":\"Тверская улица, 25/9 ст4\",\"adm_div\":[{\"id\":\"1\",\"name\":\"Россия\",\"type\":\"country\"},{\"id\":\"5349042514588558\",\"name\":\"Москва\",\"type\":\"region\"},{\"city_alias\":\"moscow\",\"flags\":{\"is_default\":true,\"is_region_center\":true},\"id\":\"4504222397630173\",\"is_default\":true,\"name\":\"Москва\",\"type\":\"city\"},{\"id\":\"4504209512726536\",\"name\":\"Тверской\",\"type\":\"district\"}],\"full_name\":\"Москва, Тверская улица, 25/9 ст4\",\"id\":\"70030076128621596\",\"name\":\"Тверская улица, 25/9 ст4\",\"point\":{\"lat\":55.767225,\"lon\":37.59947},\"purpose_name\":\"Гараж\",\"search_attributes\":{\"handling_type\":0,\"suggest_parts\":[{\"is_suggested\":false,\"text\":\"Москва, тверская, 25/9\"},{\"is_suggested\":true,\"text\":\" ст4\"}],\"suggested_text\":\"Москва, тверская, 25/9 ст4\"},\"type\":\"building\"}],\"total\":3}}", new TypeReference<Map<String, Object>>() {
                })));
        response = mockMvc.perform(get("/address?location=Тверская улица 25/9 Москва")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();
        actual = objectMapper
                .readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<List<AddressDto>>() {
                        });
        assert !actual.isEmpty();
        Assertions.assertEquals(4, actual.size());
        Assertions.assertEquals("Тверская улица", actual.get(0).getStreet());
        Assertions.assertEquals("Мамоновский переулок", actual.get(1).getStreet());
    }

    private String createRouteRequest() {
        return """
                {
                 "coordinates" : [
                     {
                         "latitude": 59.95,
                         "longitude": 30.31667,
                         "waitTime": 0
                     },
                     {
                         "latitude": 55.75583,
                         "longitude": 37.6177,
                         "waitTime": %s
                     },
                     {
                         "latitude": 57,
                         "longitude": 41
                     }
                 ]
                }
                """.formatted(WAIT_TIME);
    }


    private String createRouteRequest(String unitType, String transportServiceType) {
        return """
                {
                 "coordinates" : [
                     {
                         "latitude": 59.95,
                         "longitude": 30.31667,
                         "waitTime": 0
                     },
                     {
                         "latitude": 55.75583,
                         "longitude": 37.6177,
                         "waitTime": %s
                     },
                     {
                         "latitude": 57,
                         "longitude": 41
                     }
                 ],
                 "unit": "%s",
                 "transportServiceType": "%s"
                }
                """.formatted(WAIT_TIME, unitType, transportServiceType);
    }

    private String createRouteRequestTwoPoint(String unitType, String transportServiceType) {
        return """
                {
                 "coordinates" : [
                     {
                         "latitude": 59.95,
                         "longitude": 30.31667,
                         "waitTime": 0
                     },
                     {
                         "latitude": 55.75583,
                         "longitude": 37.6177,
                         "waitTime": %s
                     }
                 ],
                 "unit": "%s",
                 "transportServiceType": "%s"
                }
                """.formatted(WAIT_TIME, unitType, transportServiceType);
    }

    private String createRouteRequest(String unitType) {
        return createRouteRequest(unitType, REPAIR);
    }

    @SneakyThrows
    private String reversedSingleData() {
        try (var json = getClass().getClassLoader()
                .getResourceAsStream(
                        String.format("responses/%s/reverse.json", "catalog.api.2gis.com"))) {
            return new String(Objects.requireNonNull(json).readAllBytes());
        }
    }

    @SneakyThrows
    private String singleData() {
        try (var json = getClass().getClassLoader()
                .getResourceAsStream(
                        String.format("responses/%s/address.json", "catalog.api.2gis.com"))) {
            return new String(Objects.requireNonNull(json).readAllBytes());
        }
    }

    @SneakyThrows
    private String routeKilometersData(int index) {
        try (var json = getClass().getClassLoader()
                .getResourceAsStream(
                        String.format("responses/%s/route/km/%s.json",
                                "catalog.api.2gis.com", index))) {
            return new String(Objects.requireNonNull(json).readAllBytes());
        }
    }

    @SneakyThrows
    private String routeMilesData(int index) {
        try (var json = getClass().getClassLoader()
                .getResourceAsStream(
                        String.format("responses/%s/route/miles/%s.json",
                                "catalog.api.2gis.com", index))) {
            return new String(Objects.requireNonNull(json).readAllBytes());
        }
    }

    @SneakyThrows
    private String regionData() {
        try (var json = getClass().getClassLoader()
                .getResourceAsStream(
                        String.format("responses/%s/region.json", "catalog.api.2gis.com"))) {
            return new String(Objects.requireNonNull(json).readAllBytes());
        }
    }
}