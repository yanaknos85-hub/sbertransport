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
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;

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

class FuelTypeTest extends BaseIntegrationTest {
    
    private static final String ROOT_PATH = "/fuel-type";
    
    private UUID gasolineEngineType;
    private UUID dieselPowerEngineType;
    
    @BeforeEach
    void prepareTypes() {
        gasolineEngineType = engineTypeRepository.saveAndFlush(EngineType.builder()
                                                                         .title("Бензиновый")
                                                                         .build())
                                           .getId();
        dieselPowerEngineType = engineTypeRepository.saveAndFlush(EngineType.builder()
                                                                .title("Дизельный")
                                                                .build())
                                              .getId();
    }
    
    @Test
    @Description("Вид топлива ТС должен быть добавлен и получен в списке")
    @SneakyThrows
    void addAndGetRecord() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АИ-95",
                                "engineTypeId": "%s",
                                "possibleTitles": ["АИ-95","95-ый"]
                                }
                                """.formatted(gasolineEngineType)))
                .andExpect(status().isCreated());

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
                .andExpect(jsonPath("$.content.[0].title", is("АИ-95")));
    }
    
    @Test
    @Description("Наименование Вида топлива ТС должно быть обновлено")
    @SneakyThrows
    void updateSubtypeTitle() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ДИЗЕЛЬ",
                                "engineTypeId": "%s"
                                }
                                """.formatted(dieselPowerEngineType)))
                .andExpect(status().isCreated());
        
        var beforeUpdate = fuelTypeRepository.findAll();
        assertThat(beforeUpdate)
                .hasSize(1)
                .first()
                .isNotNull();
        
        var savedId = beforeUpdate.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": " МЕТАН ",
                                "engineTypeId": "%s"
                                }
                                """.formatted(dieselPowerEngineType)))
                .andExpect(status().isOk());
        
        var actualTypes = fuelTypeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(FuelType::getTitle, ft -> ft.getEngineType().getId())
                .containsExactly("МЕТАН", dieselPowerEngineType);
    }
    
    @Test
    @Description("Тип двигателя ТС должен быть обновлен")
    @SneakyThrows
    void updateSubtypeType() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АИ-95",
                                "engineTypeId": "%s"
                                }
                                """.formatted(dieselPowerEngineType)))
                .andExpect(status().isCreated());
        
        var beforeUpdate = fuelTypeRepository.findAll();
        assertThat(beforeUpdate)
                .hasSize(1)
                .first()
                .isNotNull();
        
        var savedId = beforeUpdate.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": " АИ-95 ",
                                "engineTypeId": "%s"
                                }
                                """.formatted(gasolineEngineType)))
                .andExpect(status().isOk());
        
        var actualTypes = fuelTypeRepository.findAll();
        assertThat(actualTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(FuelType::getTitle, ft -> ft.getEngineType().getId())
                .containsExactly("АИ-95", gasolineEngineType);
    }
    
    @Test
    @Description("Спосок должен быть сортирован сначала по названию Типа двигателя затем по названию Типа топлива")
    @SneakyThrows
    void cascadeSorting() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АИ-95",
                                "engineTypeId": "%s"
                                }
                                """.formatted(gasolineEngineType)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "АИ-92",
                                "engineTypeId": "%s"
                                }
                                """.formatted(gasolineEngineType)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "МЕТАН",
                                "engineTypeId": "%s"
                                }
                                """.formatted(dieselPowerEngineType)))
                .andExpect(status().isCreated());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "ДИЗЕЛЬ",
                                "engineTypeId": "%s"
                                }
                                """.formatted(dieselPowerEngineType)))
                .andExpect(status().isCreated());


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
               .andExpect(jsonPath("$.content[*].engineType.title", contains("Бензиновый", "Бензиновый", "Дизельный", "Дизельный")))
               .andExpect(jsonPath("$.content[*].title", contains("АИ-92", "АИ-95", "ДИЗЕЛЬ", "МЕТАН")));
        
    }

    @Test
    @DisplayName("Тип топлива имеющая связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var fuelType = Instancio.create(FuelType.class);
        when(fuelTypeRepository.findById(fuelType.getId())).thenReturn(Optional.of(fuelType));
        doThrow(new DataIntegrityViolationException("")).when(fuelTypeRepository).delete(fuelType);
        mockMvc.perform(delete(ROOT_PATH + "/" + fuelType.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }
}
