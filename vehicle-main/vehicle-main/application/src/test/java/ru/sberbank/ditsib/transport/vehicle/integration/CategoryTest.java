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
import ru.sberbank.ditsib.transport.vehicle.database.model.Category;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
class CategoryTest extends BaseIntegrationTest {
    
    private static final String ROOT_PATH = "/category";
    
    @Test
    @Description("Наименование категории должно быть обновлено")
    @SneakyThrows
    void updateCategory() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Мотоциплы",
                                "category": "A2"
                                }
                                """))
                .andExpect(status().isOk());
        
        var beforeUpdateCategory = categoryRepository.findAll();
        assertThat(beforeUpdateCategory)
                .hasSize(1)
                .first()
                .isNotNull();

        var savedCategoryId = beforeUpdateCategory.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedCategoryId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Мотоциплы",
                                "category": "A2"
                                }
                                """))
                .andExpect(status().isOk());
        
        var actualBrands = categoryRepository.findAll();
        assertThat(actualBrands)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Category::getTitle, Category::getCategory)
                .containsExactly("Мотоциплы", "A2");
    }
    
    @Test
    @Description("Категория ТС должна быть добавлена и получена в списке")
    @SneakyThrows
    void addAndGetCategory() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "Мотоциплы",
                                "category": "A2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Мотоциплы")));

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
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content.[0].title", is("Мотоциплы")))
                .andExpect(jsonPath("$.content.[0].category", is("A2")));
    }

    @Test
    @DisplayName("Категория имеющая связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var category = Instancio.create(Category.class);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        doThrow(new DataIntegrityViolationException("")).when(categoryRepository).delete(category);
        mockMvc.perform(delete(ROOT_PATH + "/" + category.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
