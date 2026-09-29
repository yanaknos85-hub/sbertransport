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
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;

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
class BrandTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/brand";
    
    @Test
    @Description("Наименование модели должно быть обновлено")
    @SneakyThrows
    void updateBrand() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Lada"
                                }
                                """))
                .andExpect(status().isOk());
        
        var beforeUpdateBrands = brandRepository.findAll();
        assertThat(beforeUpdateBrands)
                .hasSize(1)
                .first()
                .isNotNull();
        
        var savedBrandId = beforeUpdateBrands.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedBrandId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "VAZ"
                                }
                                """))
                .andExpect(status().isOk());
        
        var actualBrands = brandRepository.findAll();
        assertThat(actualBrands)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Brand::getTitle)
                .isEqualTo("VAZ");
    }
    
    @Test
    @Description("Модель ТС должна быть добавлена и получена в списке")
    @SneakyThrows
    void addAndGetBrandDirectory() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Lada"
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
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].title", is("Lada")));
    }
    
    @Test
    @Description("Поиск Марки ТС по частичному совпадению названия")
    @SneakyThrows
    void searchingTest() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Lada"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Laba"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Лада"
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
                                       "title": "ла"
                                   },
                                   "pageSetting": {
                                       "page": 0,
                                       "size": 10
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
               .andExpect(jsonPath("$.content.[0].title", is("Лада")));

        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "search": {
                                       "title": "lA"
                                   },
                                   "pageSetting": {
                                       "page": 0,
                                       "size": 10
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content.[*].title", containsInAnyOrder("Laba", "Lada")));
    }

    @Test
    @DisplayName("Марка имеющая связанные модели не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var brand = Instancio.create(Brand.class);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        doThrow(new DataIntegrityViolationException("")).when(brandRepository).delete(brand);
        mockMvc.perform(delete(ROOT_PATH + "/" + brand.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
