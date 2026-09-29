package ru.sber.transport.telemechanic.controller.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ENGINEER_CORP_CLIENT;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@EmbeddedPostgres
class MedicRequestControllerImplTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @Test
    @DisplayName("Получение реестра для всех организаций")
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @SneakyThrows
    void getRegistryForAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        mockMvc.perform(post("/medic-request/report/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                         	"fieldSet": [
                                         		"EWB_HUMAN_READABLE_ID"
                                         	]
                                         }
                                         """)
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(3));
    }
    
    @Test
    @DisplayName("Получение реестра для определенной организации")
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @SneakyThrows
    void getRegistryForSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ENGINEER_CORP_CLIENT.name());
        mockMvc.perform(post("/medic-request/report/self-organization")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                         	"fieldSet": [
                                         		"EWB_HUMAN_READABLE_ID"
                                         	]
                                         }
                                         """)
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(3));
    }
}
