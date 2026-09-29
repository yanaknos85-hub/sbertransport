package ru.sberbank.ditsib.transport.vehicle.integration;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;

import java.util.Optional;
import java.util.UUID;

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

class TypeTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/type";

    @Test
    @Description("Вид ТС должен быть добавлен и получен в списке")
    @SneakyThrows
    void addAndGettypeDirectory() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Служебный"
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
                .andExpect(jsonPath("$.content.[0].title", is("Служебный")));
    }

    @Test
    @Description("Наименование Вида ТС должно быть обновлено")
    @SneakyThrows
    void updateType() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Служебный"
                                }
                                """))
                .andExpect(status().isOk());

        var beforeUpdateTypes = typeRepository.findAll();
        assertThat(beforeUpdateTypes)
                .hasSize(1)
                .first()
                .isNotNull();

        var savedTypeId = beforeUpdateTypes.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedTypeId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": " Личный "
                                }
                                """))
                .andExpect(status().isOk());

        var actualTypes = typeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Type::getTitle)
                .isEqualTo("Личный");
    }

    @Test
    @Description("Поиск Типа ТС по частичному совпадению названия")
    @SneakyThrows
    void searchingTest() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Служебный"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Неслужебный"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "слжебный"
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
                                        "title": "служ"
                                    },
                                    "pageSetting": {
                                        "page": 0,
                                        "size": 10
                                     }
                                }
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]", hasSize(2)))
                .andExpect(jsonPath("$.content.[*].title", contains("Неслужебный", "Служебный")));
    }

    @Test
    @DisplayName("Тип ТС имеющий связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var type = Instancio.create(Type.class);
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        doThrow(new DataIntegrityViolationException("")).when(typeRepository).delete(type);
        mockMvc.perform(delete(ROOT_PATH + "/" + type.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
