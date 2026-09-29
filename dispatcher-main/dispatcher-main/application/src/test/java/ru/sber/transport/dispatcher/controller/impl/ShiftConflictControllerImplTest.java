package ru.sber.transport.dispatcher.controller.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.DispatcherApplication;
import ru.sber.transport.dispatcher.database.dao.ShiftConflictRepository;
import ru.sber.transport.dispatcher.database.model.ConflictReason;
import ru.sber.transport.dispatcher.database.model.ShiftConflict;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@EmbeddedPostgres
@SpringBootTest(classes = DispatcherApplication.class)
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера конфликтов смен")
@Transactional
class ShiftConflictControllerImplTest extends KafkaTest {

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ShiftConflictRepository shiftConflictRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private Key key;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Проверка получения конфликтов смен с данными")
    void test_getAll_withData() throws Exception {
        // Given: создаем конфликты в базе
        ShiftConflict conflict1 = shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("123456")
                .stateNumber("А123ВС777")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now())
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        ShiftConflict conflict2 = shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("789012")
                .stateNumber("Б456ХД999")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        ShiftConflict conflict3 = shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("345678")
                .stateNumber("В789ЮЕ000")
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().minusDays(2))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        // When: выполняем GET запрос
        mockMvc.perform(get("/shift-conflicts/")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].routeId").value(conflict1.getRouteId()))
                .andExpect(jsonPath("$.content[1].routeId").value(conflict2.getRouteId()))
                .andExpect(jsonPath("$.content[2].routeId").value(conflict3.getRouteId()));
    }

    @Test
    @DisplayName("Проверка фильтрации по табельному номеру")
    void test_getAll_filterByPersonnelNumber() throws Exception {
        // Given: создаем конфликты с разными табельными номерами
        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("111111")
                .stateNumber("А123ВС777")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("222222")
                .stateNumber("Б456ХД999")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        // When: фильтруем по табельному номеру
        mockMvc.perform(get("/shift-conflicts/")
                        .param("personnelNumber", "111111")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].personnelNumber").value("111111"));
    }

    @Test
    @DisplayName("Проверка фильтрации по государственному номеру")
    void test_getAll_filterByStateNumber() throws Exception {
        // Given: создаем конфликты с разными госномерами
        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("111111")
                .stateNumber("А123ВС777")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("222222")
                .stateNumber("Б456ХД999")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        // When: фильтруем по государственному номеру
        mockMvc.perform(get("/shift-conflicts/")
                        .param("stateNumber", "А123ВС777")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].stateNumber").value("А123ВС777"));
    }

    @Test
    @DisplayName("Проверка фильтрации по причине конфликта")
    void test_getAll_filterByConflictReason() throws Exception {
        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("111111")
                .stateNumber("А123ВС777")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(UUID.randomUUID().toString())
                .personnelNumber("222222")
                .stateNumber("Б456ХД999")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.VEHICLE_NOT_FOUND)
                .build());

        // When: фильтруем по государственному номеру
        mockMvc.perform(get("/shift-conflicts/")
                        .param("conflictReason", "VEHICLE_NOT_FOUND")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].stateNumber").value("Б456ХД999"));
    }

    @Test
    @DisplayName("Удаление конфликта")
    void test_delete() throws Exception {
        var routeId = UUID.randomUUID().toString();

        shiftConflictRepository.saveAndFlush(ShiftConflict.builder()
                .routeId(routeId)
                .personnelNumber("111111")
                .stateNumber("А123ВС777")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .conflictReason(ConflictReason.DRIVER_INACTIVE)
                .build());

        mockMvc.perform(delete("/shift-conflicts/"+routeId+"/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID))))
                .andExpect(status().isOk());

        Assertions.assertEquals(0, shiftConflictRepository.count());
    }
}
