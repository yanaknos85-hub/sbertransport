package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

@DisplayName("Тест проверки контроллера Размера колеса ТС")
class WheelSizeTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/wheel-size";

    @Test
    @SneakyThrows
    void addAndGet() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "185/55R15 81V"
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
                                        "size": 10
                                     }
                                }
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].title", is("185/55R15 81V")));
    }

    @Test
    @SneakyThrows
    void update() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "185/55R15 81V"
                                }
                                """))
                .andExpect(status().isOk());

        var beforeUpdateTypes = wheelSizeRepository.findAll();
        assertThat(beforeUpdateTypes)
                .hasSize(1)
                .first()
                .isNotNull();

        var savedEntity = beforeUpdateTypes.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedEntity)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": " 185/55R15 20V "
                                }
                                """))
                .andExpect(status().isOk());

        var actualTypes = wheelSizeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(WheelSize::getTitle)
                .isEqualTo("185/55R15 20V");
    }

    @Test
    @SneakyThrows
    void searching() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "185/55R15 81V"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "185/55R17 20V"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "185/55R15 82V"
                                }
                                """))
                .andExpect(status().isOk());


        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "search": {
                                        "title": "185/55R15"
                                    },
                                    "pageSetting": {
                                        "page": 0,
                                        "size": 10
                                     }
                                }
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]", hasSize(2)))
                .andExpect(jsonPath("$.content.[*].title", containsInAnyOrder("185/55R15 82V", "185/55R15 81V")));
    }

    @Test
    @SneakyThrows
    void deleteEntityWithRelations() {
        var entity = Instancio.create(WheelSize.class);
        when(wheelSizeRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        doThrow(new DataIntegrityViolationException("")).when(wheelSizeRepository).delete(entity);
        mockMvc.perform(delete(ROOT_PATH + "/" + entity.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
