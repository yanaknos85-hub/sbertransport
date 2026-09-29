package ru.sberbank.ditsib.transport.vehicle.integration;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype;
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

class SubtypeTest extends BaseIntegrationTest {
    
    private static final String ROOT_PATH = "/subtype";
    
    private UUID typePersonalId;
    private UUID typeOfficialId;
    
    @BeforeEach
    void prepareTypes() {
        typePersonalId = typeRepository.saveAndFlush(Type.builder()
                                                         .title("Личный")
                                                         .build())
                                       .getId();
        typeOfficialId = typeRepository.saveAndFlush(Type.builder()
                                                         .title("Служебный")
                                                         .build())
                                       .getId();
    }
    
    @Test
    @Description("Подвид ТС должен быть добавлен и получен в списке")
    @SneakyThrows
    void addAndGetRecord() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "СТС",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
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
                .andExpect(jsonPath("$.content.[0].title", is("СТС")));
    }
    
    @Test
    @Description("Наименование Подвида ТС должно быть обновлено")
    @SneakyThrows
    void updateSubtypeTitle() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ППКО",
                                "typeId": "%s"
                                }
                                """.formatted(typeOfficialId)))
                .andExpect(status().isOk());

        var beforeUpdateTypes = subtypeRepository.findAll();
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
                                "title": " ТОН ",
                                "typeId": "%s"
                                }
                                """.formatted(typeOfficialId)))
                .andExpect(status().isOk());

        var actualTypes = subtypeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Subtype::getTitle, st -> st.getType().getId())
                .containsExactly("ТОН", typeOfficialId);
    }
    
    @Test
    @Description("Вид Подвида ТС должно быть обновлено")
    @SneakyThrows
    void updateSubtypeType() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ППКО",
                                "typeId": "%s"
                                }
                                """.formatted(typeOfficialId)))
                .andExpect(status().isOk());
        
        var beforeUpdateTypes = subtypeRepository.findAll();
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
                                "title": " ППКО ",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
                .andExpect(status().isOk());

        var actualTypes = subtypeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Subtype::getTitle, st -> st.getType().getId())
                .containsExactly("ППКО", typePersonalId);
    }
    
    @Test
    @Description("Спосок должен быть сортирован сначала по Виду затем по подвиду ТС")
    @SneakyThrows
    void cascadeSorting() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Ба",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Аа",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                         "title": "Бб",
                                         "typeId": "%s"
                                         }
                                         """.formatted(typeOfficialId)))
               .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Аб",
                                "typeId": "%s"
                                }
                                """.formatted(typeOfficialId)))
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
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].type.title", contains("Личный", "Личный", "Служебный", "Служебный")))
                .andExpect(jsonPath("$.content[*].title", contains("Аа", "Ба", "Аб", "Бб")));
    }
    
    @Test
    @Description("Поиск Подтипа ТС по частичному совпадению названия")
    @SneakyThrows
    void searchingTest() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "какой-то подтип 1",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "какой-то тип тут должен быть",
                                "typeId": "%s"
                                }
                                """.formatted(typePersonalId)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "какой-то подтип 2",
                                "typeId": "%s"
                                }
                                """.formatted(typeOfficialId)))
                .andExpect(status().isOk());


        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "search": {
                                       "title": "какой-то подт"
                                   },
                                   "pageSetting": {
                                       "page": 0,
                                       "size": 10
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", contains("какой-то подтип 1", "какой-то подтип 2")));
    }

    @Test
    @DisplayName("Подтип имеющий связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var subtype = Instancio.create(Subtype.class);
        when(subtypeRepository.findById(subtype.getId())).thenReturn(Optional.of(subtype));
        doThrow(new DataIntegrityViolationException("")).when(subtypeRepository).delete(subtype);
        mockMvc.perform(delete(ROOT_PATH + "/" + subtype.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
    
}
