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
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive;

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
class DriveTest extends BaseIntegrationTest {
    
    private static final String ROOT_PATH = "/drive";
    
    @Test
    @Description("Наименование привода должно быть обновлено")
    @SneakyThrows
    void update() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "передний"
                                }
                                """))
                .andExpect(status().isOk());
        
        var beforeUpdatedDrives = driveRepository.findAll();
        assertThat(beforeUpdatedDrives)
                .hasSize(1)
                .first()
                .isNotNull();
        
        var savedEngineTypeId = beforeUpdatedDrives.get(0).getId();
        mockMvc.perform(put(ROOT_PATH + "/" + savedEngineTypeId)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "задний"
                                }
                                """))
                .andExpect(status().isOk());
        
        var actualEnineTypes = driveRepository.findAll();
        assertThat(actualEnineTypes)
                .hasSize(1)
                .first()
                .isNotNull()
                .extracting(Drive::getTitle)
                .isEqualTo("задний");
    }
    
    @Test
    @Description("Привод ТС должен быть добавлен и получен в списке")
    @SneakyThrows
    void addAndGet() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "полный"
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
               .andExpect(jsonPath("$.content.[0].title", is("полный")));
    }

    @Test
    @DisplayName("Привод имеющий связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var drive = Instancio.create(Drive.class);
        when(driveRepository.findById(drive.getId())).thenReturn(Optional.of(drive));
        doThrow(new DataIntegrityViolationException("")).when(driveRepository).delete(drive);
        mockMvc.perform(delete(ROOT_PATH + "/" + drive.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }

}
