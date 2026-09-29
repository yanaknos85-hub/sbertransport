package ru.sber.transport.trip.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера настроек отображаемых столбцов")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class ColumnSettingsControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private ConsentFunction function;

    private DispatcherRecord dispatcher;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData() {
        AuthorizeUtils.authorize(manager);
        when(function.apply(any())).thenReturn(true);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        var contractor = new ContractorsRecord();
        contractor.setId(UUID.randomUUID());
        contractor.setDigitId(BigInteger.ONE);
        contractor.setAutoassign(true);

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        dispatcher = new DispatcherRecord();
        dispatcher.setId(UUID.randomUUID());
        dispatcher.setHumanReadableId("DS-0001-0001");
        dispatcher.setLastName("LastNameDispatcher");
        dispatcher.setFirstName("FirstNameDispatcher");
        dispatcher.setPatronymic("PatronymicDispatcher");
        dispatcher.setPhone("+70327749923");
        dispatcher.setEmail("disp@mail.ru");
        dispatcher.setContractorId(contractor.getId());

        dslContext.insertInto(Tables.DISPATCHER).set(dispatcher).execute();
    }

    @Test
    @DisplayName("Проверка создания настройки отображаемых столбцов")
    void createColumnSettingsTest() throws Exception {
        var content = """
                {
                    "setting1": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting2": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/settings/reports/columns/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.containsKey("setting1"));
        Assertions.assertTrue(result.containsKey("setting2"));
    }

    @Test
    @DisplayName("Проверка изменения настройки отображаемых столбцов")
    void updateColumnSettingsTest() throws Exception {
        var existingSettings = """
                {
                    "setting1": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting2": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;

        dslContext.insertInto(Tables.COLUMN_SETTINGS)
                .set(new ColumnSettingsRecord(dispatcher.getId(), JSON.json(existingSettings)))
                .execute();

        var content = """
                {
                    "setting3": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting4": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.put("/settings/reports/columns/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        var result = dslContext.select(Tables.COLUMN_SETTINGS.SETTING).from(Tables.COLUMN_SETTINGS)
                .where(Tables.COLUMN_SETTINGS.USER_ID.eq(dispatcher.getId())).fetchOne();
        assert result != null;
        var resultMap = objectMapper.readValue(result.value1().data(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertNotNull(resultMap);
        Assertions.assertEquals(2, resultMap.size());
        Assertions.assertFalse(resultMap.containsKey("setting1"));
        Assertions.assertFalse(resultMap.containsKey("setting2"));
        Assertions.assertTrue(resultMap.containsKey("setting3"));
        Assertions.assertTrue(resultMap.containsKey("setting4"));
    }

    @Test
    @DisplayName("Проверка удаления настройки отображаемых столбцов")
    void deleteColumnSettingsTest() throws Exception {
        var existingSettings = """
                {
                    "setting1": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting2": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;

        dslContext.insertInto(Tables.COLUMN_SETTINGS)
                .set(new ColumnSettingsRecord(dispatcher.getId(), JSON.json(existingSettings)))
                .execute();

        mockMvc.perform(MockMvcRequestBuilders.delete("/settings/reports/columns/")
                .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(MockMvcResultMatchers.status().isOk());

        var result = dslContext.select(Tables.COLUMN_SETTINGS.SETTING).from(Tables.COLUMN_SETTINGS)
                .where(Tables.COLUMN_SETTINGS.USER_ID.eq(dispatcher.getId())).fetchOne();
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("Проверка получения настройки отображаемых столбцов")
    void getColumnSettingsTest() throws Exception {
        var existingSettings = """
                {
                    "setting1": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting2": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;

        dslContext.insertInto(Tables.COLUMN_SETTINGS)
                .set(new ColumnSettingsRecord(dispatcher.getId(), JSON.json(existingSettings)))
                .execute();

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/settings/reports/columns/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var result = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.containsKey("setting1"));
        Assertions.assertTrue(result.containsKey("setting2"));
    }

    @Test
    @DisplayName("Проверка получения настройки отображаемых столбцов (настройка не найдена)")
    void getColumnSettingsNotFoundTest() throws Exception {
        var existingSettings = """
                {
                    "setting1": {
                            "param1":"value1",
                            "param2":"value2"
                        },
                    "setting2": {
                            "param1":"value1",
                            "param2":"value2"
                        }
                }
                """;

        dslContext.insertInto(Tables.COLUMN_SETTINGS)
                .set(new ColumnSettingsRecord(dispatcher.getId(), JSON.json(existingSettings)))
                .execute();

        mockMvc.perform(MockMvcRequestBuilders.get("/settings/reports/columns/")
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
