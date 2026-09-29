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
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
class EngineTypeTest extends BaseIntegrationTest {
    
    private static final String ROOT_PATH = "/engine-type";
    
    @Test
    @Description("Наименование типа двигятеля должно быть обновлено")
    @SneakyThrows
    void update() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "БЕНЗИН"
                                }
                                """))
                .andExpect(status().isOk());
        
        var beforeUpdateEngineTypes = engineTypeRepository.findAll();
        assertThat(beforeUpdateEngineTypes)
                .hasSize(1)
                .first()
                .isNotNull();
        
        var savedEngineTypeId = beforeUpdateEngineTypes.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedEngineTypeId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ГИБРИДНЫЙ"
                                }
                                """))
                .andExpect(status().isOk());
        
        var actualEnineTypes = engineTypeRepository.findAll();
        assertThat(actualEnineTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(EngineType::getTitle)
                .isEqualTo("ГИБРИДНЫЙ");
    }
    
    @Test
    @Description("Тип двигателя ТС должна быть добавлена и получена в списке")
    @SneakyThrows
    void addAndGet() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ГИБРИДНЫЙ"
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
               .andExpect(jsonPath("$.content.[0].title", is("ГИБРИДНЫЙ")));
    }

    @Test
    @DisplayName("Тип двигателя имеющая связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var engineType = Instancio.create(EngineType.class);
        when(engineTypeRepository.findById(engineType.getId())).thenReturn(Optional.of(engineType));
        doThrow(new DataIntegrityViolationException("")).when(engineTypeRepository).delete(engineType);
        mockMvc.perform(delete(ROOT_PATH + "/" + engineType.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
