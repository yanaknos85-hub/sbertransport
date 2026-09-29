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
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model;

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

class ModelTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/model";

    private UUID brandChangan;
    private UUID brandMoskvich;

    @BeforeEach
    void prepareTypes() {
        brandChangan = brandRepository.saveAndFlush(Brand.builder()
                                                        .title("Changan")
                .build()).getId();
        brandMoskvich = brandRepository.saveAndFlush(Brand.builder()
                                                        .title("Москвич")
                .build()).getId();
    }

    @Test
    @Description("Модель ТС должна быть добавлена и получена в списке")
    @SneakyThrows
    void addAndGetRecord() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "V60",
                                "brandId": "%s"
                                }
                                """.formatted(brandChangan)))
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
               .andExpect(jsonPath("$.content.[0].title", is("V60")));
    }

    @Test
    @Description("Наименование Модели ТС должно быть обновлено")
    @SneakyThrows
    void updateModelTitle() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "2733",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());

        var beforeUpdateTypes = modelRepository.findAll();
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
                                "title": " 2136 ",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());

        var actualTypes = modelRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Model::getTitle, st -> st.getBrand().getId())
                .containsExactly("2136", brandMoskvich);
    }

    @Test
    @Description("Марка модели ТС должна быть обновлена")
    @SneakyThrows
    void updateBrandOfModelType() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "V90",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());

        var beforeUpdateTypes = modelRepository.findAll();
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
                                "title": " V90 ",
                                "brandId": "%s"
                                }
                                """.formatted(brandChangan)))
                .andExpect(status().isOk());

        var actualTypes = modelRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Model::getTitle, st -> st.getBrand().getId())
                .containsExactly("V90", brandChangan);
    }

    @Test
    @Description("Спосок должен быть сортирован сначала по Марке далее по Модели ТС")
    @SneakyThrows
    void cascadeSorting() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Ав",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "А12",
                                "brandId": "%s"
                                }
                                """.formatted(brandChangan)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Аб",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "V90",
                                "brandId": "%s"
                                }
                                """.formatted(brandChangan)))
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
               .andExpect(jsonPath("$.content[*].brand.title", contains("Changan", "Changan", "Москвич", "Москвич")))
               .andExpect(jsonPath("$.content[*].title", contains("V90", "А12", "Аб", "Ав")));

    }

    @Test
    @Description("Поиск Модели ТС по частичному совпадению названия")
    @SneakyThrows
    void searchingTest() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АаАва",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АаАба",
                                "brandId": "%s"
                                }
                                """.formatted(brandChangan)))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Ааива",
                                "brandId": "%s"
                                }
                                """.formatted(brandMoskvich)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "search": {
                                       "title": "аАа"
                                   },
                                   "pageSetting": {
                                       "page": 0,
                                       "size": 10
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", contains("АаАба", "АаАва")));

    }

    @Test
    @DisplayName("Модель имеющая связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var model = Instancio.create(Model.class);
        when(modelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        doThrow(new DataIntegrityViolationException("")).when(modelRepository).delete(model);
        mockMvc.perform(delete(ROOT_PATH + "/" + model.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }

}
