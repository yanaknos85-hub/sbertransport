package ru.sber.transport.driver_track.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.dto.RouteType;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.repository.ExpectedRouteRepository;
import ru.sber.transport.driver_track.repository.RouteRepository;
import ru.sber.transport.driver_track.service.GeoClient;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@SpringBootTest()
@AutoConfigureMockMvc
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@MockitoBean(types = GeoClient.class)
@MockitoBean(types = JwtDecoder.class)
@MockitoBean(types = GeoServiceGrpc.GeoServiceBlockingStub.class)
@DisplayName("Проверка контроллера координат")
@ActiveProfiles("test")
public class RouteControllerImplTest {

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ExpectedRouteRepository expectedRouteRepository;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService, "ROLE_DRIVER_CONTRACTOR");
    }

    @Test
    @DisplayName("Сохранение координат")
    void savePointInfoTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var latitude = Instancio.create(Double.class);
        var longitude = Instancio.create(Double.class);

        mockMvc.perform(
                        post("/")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                              "latitude":"%s",
                                              "longitude":"%s"
                                          }
                                        """.formatted(latitude, longitude)))
                .andExpectAll(
                        status().isOk()
                );

        var coordinates = coordinateRepository.findAll();
        assertEquals(1, coordinates.size());

        var coordinate = coordinates.getFirst();
        assertEquals(driver.getActiveTripId(), coordinate.getTripId());
        assertEquals(latitude, coordinate.getLatitude());
        assertEquals(longitude, coordinate.getLongitude());
    }

    @Test
    @DisplayName("Получение фактического маршрута")
    void getRouteTest() throws Exception {
        var route = Instancio.create(RouteDTO.class);
        var routeJson = JSON.valueOf(objectMapper.writeValueAsString(route));
        var routeRecord = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), routeJson, RouteSource.FORMULA.name());
        routeRepository.save(routeRecord);

        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var result = mockMvc.perform(
                        get("/?tripId=" + routeRecord.getTripId() + "&routeType=" + RouteType.FACT)
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk()
                ).andReturn();

        var actual = objectMapper.readValue(result.getResponse().getContentAsString(), RouteDTO.class);
        assertEquals(route.getDistance(), actual.getDistance());

        var expectedSegments = route.getSegments();
        var actualSegments = actual.getSegments();
        assertEquals(expectedSegments.size(), actualSegments.size());

        for (var i = 0; i < actualSegments.size(); i++) {
            var expectedSegment = expectedSegments.get(i);
            var actualSegment = actualSegments.get(i);
            assertEquals(expectedSegment.getDistance(), actualSegment.getDistance());

            var expectedPoints = expectedSegment.getPoints();
            var actualPoints = actualSegment.getPoints();
            assertEquals(expectedPoints.size(), actualPoints.size());
            assertEquals(expectedPoints.getFirst().getLongitude(), actualPoints.getFirst().getLongitude());
            assertEquals(expectedPoints.getFirst().getLatitude(), actualPoints.getFirst().getLatitude());
            for (var j = 1; j < expectedPoints.size(); j++) {
                assertEquals(expectedPoints.get(j).getLongitude(), actualPoints.get(j).getLongitude());
                assertEquals(expectedPoints.get(j).getLatitude(), actualPoints.get(j).getLatitude());
            }
        }

    }

    @Test
    @DisplayName("Пакетное сохранение координат")
    void saveBatchPointsTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var tripId = driver.getActiveTripId();

        mockMvc.perform(
                        post("/batch")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": "%s",
                                          "points": [
                                            {"latitude": 55.7558, "longitude": 37.6173, "timestamp": "2026-07-12T10:00:00Z"},
                                            {"latitude": 59.9343, "longitude": 30.3351, "timestamp": "2026-07-12T10:05:00Z"}
                                          ]
                                        }
                                        """.formatted(tripId)))
                .andExpectAll(
                        status().isOk()
                );

        var coordinates = coordinateRepository.findAll();
        assertEquals(2, coordinates.size());

        var first = coordinates.get(0);
        assertEquals(tripId, first.getTripId());
        assertEquals(55.7558, first.getLatitude());
        assertEquals(37.6173, first.getLongitude());

        var second = coordinates.get(1);
        assertEquals(tripId, second.getTripId());
        assertEquals(59.9343, second.getLatitude());
        assertEquals(30.3351, second.getLongitude());
    }

    @Test
    @DisplayName("Пакетное сохранение координат с отрицательными координатами")
    void saveBatchPointsNegativeCoordinatesTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var tripId = driver.getActiveTripId();

        mockMvc.perform(
                        post("/batch")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": "%s",
                                          "points": [
                                            {"latitude": -33.8688, "longitude": 151.2093, "timestamp": "2026-07-12T10:00:00Z"}
                                          ]
                                        }
                                        """.formatted(tripId)))
                .andExpectAll(
                        status().isOk()
                );

        var coordinates = coordinateRepository.findAll();
        assertEquals(1, coordinates.size());
        assertEquals(-33.8688, coordinates.get(0).getLatitude());
        assertEquals(151.2093, coordinates.get(0).getLongitude());
    }

    @Test
    @DisplayName("Пакетное сохранение координат с пустым списком точек должно вернуть 400")
    void saveBatchPointsEmptyPointsTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        mockMvc.perform(
                        post("/batch")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": "%s",
                                          "points": []
                                        }
                                        """.formatted(driver.getActiveTripId())))
                .andExpectAll(
                        status().isBadRequest()
                );
    }

    @Test
    @DisplayName("Пакетное сохранение координат с null tripId должно вернуть 400")
    void saveBatchPointsNullTripIdTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        mockMvc.perform(
                        post("/batch")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": null,
                                          "points": [
                                            {"latitude": 55.7558, "longitude": 37.6173, "timestamp": "2026-07-12T10:00:00Z"}
                                          ]
                                        }
                                        """))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.problems[0].field").value("tripId")
                );
    }

    @Test
    @DisplayName("Пакетное сохранение координат с отсутствующим пользователем должно вернуть 401")
    void saveBatchPointsUnauthorizedTest() throws Exception {
        mockMvc.perform(
                        post("/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": "550e8400-e29b-41d4-a716-446655440000",
                                          "points": [
                                            {"latitude": 55.7558, "longitude": 37.6173, "timestamp": "2026-07-12T10:00:00Z"}
                                          ]
                                        }
                                        """))
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    @DisplayName("Пакетное сохранение координат с миллисекундным timestamp конвертируется и сохраняется")
    void saveBatchPointsMillisecondTimestampTest() throws Exception {
        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var tripId = driver.getActiveTripId();
        var msTimestamp = "1787295660000";

        mockMvc.perform(
                        post("/batch")
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "tripId": "%s",
                                          "points": [
                                            {"latitude": 55.7558, "longitude": 37.6173, "timestamp": %s}
                                          ]
                                        }
                                        """.formatted(tripId, msTimestamp)))
                .andExpectAll(
                        status().isOk()
                );

        var coordinates = coordinateRepository.findAll();
        assertEquals(1, coordinates.size());

        var coord = coordinates.get(0);
        assertEquals(tripId, coord.getTripId());
        assertEquals(55.7558, coord.getLatitude());
        assertEquals(37.6173, coord.getLongitude());
        // Проверяем что timestamp конвертирован в 2026 год, а не 58607
        assertEquals(2026, coord.getTime().getYear());
    }

    @Test
    @DisplayName("Получение планового маршрута")
    void getExpectedRouteTest() throws Exception {
        var route = Instancio.create(RouteDTO.class);
        var routeJson = JSON.valueOf(objectMapper.writeValueAsString(route));
        var routeRecord = new ExpectedRouteRecord(UUID.randomUUID(), UUID.randomUUID(), routeJson, RouteSource.FORMULA.name());
        expectedRouteRepository.save(routeRecord);

        var driver = new DriverMessageRecord(UUID.randomUUID(), true, true, UUID.randomUUID(), true, true);
        driverRepository.save(driver);

        var result = mockMvc.perform(
                        get("/?tripId=" + routeRecord.getTripId() + "&routeType=" + RouteType.EXPECTED)
                                .with(jwt().jwt(builder -> builder.jti(driver.getId().toString()).claim("scope", "DRIVER"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_DRIVER_CONTRACTOR")))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk()
                ).andReturn();

        var actual = objectMapper.readValue(result.getResponse().getContentAsString(), RouteDTO.class);
        assertEquals(route.getDistance(), actual.getDistance());

        var expectedSegments = route.getSegments();
        var actualSegments = actual.getSegments();
        assertEquals(expectedSegments.size(), actualSegments.size());

        for (var i = 0; i < actualSegments.size(); i++) {
            var expectedSegment = expectedSegments.get(i);
            var actualSegment = actualSegments.get(i);
            assertEquals(expectedSegment.getDistance(), actualSegment.getDistance());

            var expectedPoints = expectedSegment.getPoints();
            var actualPoints = actualSegment.getPoints();
            assertEquals(expectedPoints.size(), actualPoints.size());
            assertEquals(expectedPoints.getFirst().getLongitude(), actualPoints.getFirst().getLongitude());
            assertEquals(expectedPoints.getFirst().getLatitude(), actualPoints.getFirst().getLatitude());
            for (var j = 1; j < expectedPoints.size(); j++) {
                assertEquals(expectedPoints.get(j).getLongitude(), actualPoints.get(j).getLongitude());
                assertEquals(expectedPoints.get(j).getLatitude(), actualPoints.get(j).getLatitude());
            }
        }

    }
}
