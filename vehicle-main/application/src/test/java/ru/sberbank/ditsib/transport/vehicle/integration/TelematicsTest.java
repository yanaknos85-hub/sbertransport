package ru.sberbank.ditsib.transport.vehicle.integration;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author skakun-a
 */

class TelematicsTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/telematics";

    @Test
    @Description("Телематика должна быть добавлена и получен")
    @SneakyThrows
    void addAndGetTelematics() {
        var resultAsString = mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);

        var telematicsDto = objectMapper.readValue(resultAsString, TelematicsDto.class);

        assertThat(telematicsDto)
                .isNotNull()
                .extracting(TelematicsDto::imei, TelematicsDto::title)
                .containsExactly("12345678901111", "Сантел-навигация");


        mockMvc.perform(get(ROOT_PATH + "/" + telematicsDto.id().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Сантел-навигация")))
                .andExpect(jsonPath("$.imei", is("12345678901111")));
    }

    @Test
    @Description("Наименование Телематики должно быть обновлено")
    @SneakyThrows
    void updateTelematics() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk());

        var beforeUpdateTelematicsList = telematicsRepository.findAll();
        assertThat(beforeUpdateTelematicsList)
                .hasSize(1)
                .first()
                .isNotNull();

        var savedTelematicsId = beforeUpdateTelematicsList.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedTelematicsId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk());

        var actualTelematics = telematicsRepository.findAll();
        assertThat(actualTelematics)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Telematics::getTitle)
                .isEqualTo("Сантел навигация");
    }

    @Test
    @Description("Выдача всех записей Телематики должен быть в алфавитном порядке по названию.")
    @SneakyThrows
    void searchingTest() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Бан-навигация",
                                "imei": "12345678901112"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Авто-навигация",
                                "imei": "12345678921112"
                                }
                                """))
                .andExpect(status().isOk());


        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "pageSetting": {
                                        "page": 0,
                                        "size": 2
                                     }
                                }
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]", hasSize(2)))
                .andExpect(jsonPath("$.content.[*].title",
                        contains("Авто-навигация", "Бан-навигация")))
                .andExpect(jsonPath("$.content.[*].imei",
                        contains("12345678921112", "12345678901112")));

        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]", hasSize(3)))
                .andExpect(jsonPath("$.content.[*].title",
                        contains("Авто-навигация", "Бан-навигация", "Сантел-навигация")))
                .andExpect(jsonPath("$.content.[*].imei",
                        contains("12345678921112", "12345678901112", "12345678901111")));
    }

    @Test
    @DisplayName("Удаление должно проходить успешно при отсутствии связанных записей")
    @SneakyThrows
    void deleteEntity() {
        var resultAsString = mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);

        var telematicsDto = objectMapper.readValue(resultAsString, TelematicsDto.class);

        mockMvc.perform(delete(ROOT_PATH + "/" + telematicsDto.id().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Телематика, имеющая связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var telematics = Instancio.create(Telematics.class);
        when(telematicsRepository.findById(telematics.getId())).thenReturn(Optional.of(telematics));
        doThrow(new DataIntegrityViolationException("")).when(telematicsRepository).delete(telematics);
        mockMvc.perform(delete(ROOT_PATH + "/" + telematics.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));
    }

    @ParameterizedTest
    @MethodSource
    @SneakyThrows
    void createTelemetrics(String content) {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict());
    }

    private static Stream<Arguments> createTelemetrics() {
        return Stream.of(
                Arguments.of("""
                        {
                            "title": "Навигация",
                            "imei": "12345678901111"
                        }
                        """),
                Arguments.of("""
                        {
                            "title": "Сантел-навигация",    
                            "imei": "12345678901112"
                        }
                        """),
                Arguments.of("""
                        {
                            "title": "Сантел-НАВИГАЦИЯ",
                            "imei": "12345678901112"
                        }
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource
    @SneakyThrows
    void updateTelemetrics(String content, ResultMatcher resultMatcher) {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901111"
                                }
                                """))
                .andExpect(status().isOk());
        var resultAsString = mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Сантел-навигация2",
                                "imei": "12345678901112"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        var telematicsDto = objectMapper.readValue(resultAsString, TelematicsDto.class);

        mockMvc.perform(put(ROOT_PATH + "/" + telematicsDto.id())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(resultMatcher);
    }

    private static Stream<Arguments> updateTelemetrics() {
        return Stream.of(
                Arguments.of("""
                                {
                                "title": "Сантел-НАВИГАЦИЯ4",
                                "imei": "12345678901112"
                                }
                                """,
                        status().isOk()),
                Arguments.of("""
                                {
                                "title": "Сантел-НАВИГАЦИЯ",
                                "imei": "12345678901112"
                                }
                                """,
                        status().isConflict()),

                Arguments.of("""
                                {
                                "title": "Сантел-навигация",
                                "imei": "12345678901112"
                                }
                                """,
                        status().isConflict()),

                Arguments.of("""
                                {
                                "title": "Сантел-НАВИГАЦИЯ6",
                                "imei": "12345678901111"
                                }
                                """,
                        status().isConflict()),

                Arguments.of("""
                                {
                                "title": "Сантел-навигация4",
                                "imei": "12345678901112"
                                }
                                """,
                        status().isOk())
        );
    }
}
