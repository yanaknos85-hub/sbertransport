package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.enumerate.Role;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql",
        "/scripts/organization_medical_license.sql",
        "/scripts/ewb_contract.sql",
        "/scripts/fleet_owner_organization.sql",
        "/scripts/ewb_tariff.sql"
})
class OrganizationIntegrationTest {
    
    @MockitoBean
    protected AuthorizationManager<?> manager;
    @Autowired
    protected MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(manager, Role.ROLE_DISPATCHER_SUPPORT_SERVICE.name());
    }
    
    @Test
    @SneakyThrows
    void getAllDepartamentWithTariff() {
        mockMvc.perform(get("/organization/621c288d-e348-46e5-a319-cbf61ef1e396/departments-with-tariffs")
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(
                                                   new SimpleGrantedAuthority(Role.ROLE_DISPATCHER_SUPPORT_SERVICE.name())
                                                       )
                                     )
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[*]", Matchers.hasSize(2)))
               .andExpect(jsonPath("$[*].id", Matchers.containsInAnyOrder(
                       "489a0090-1819-4c60-a611-572ea115c6a4", "c3820eb5-e6a5-4fb6-a5c9-1e88a0e5055d"
                                                                         )))
               .andExpect(jsonPath("$[*].departmentName", Matchers.containsInAnyOrder(
                       "Test2", "Test_Child_1"
                                                                                     )))
               .andExpect(jsonPath("$[*].parentId", Matchers.containsInAnyOrder(
                       null, "489a0090-1819-4c60-a611-572ea115c6a4"
                                                                               )));
        
    }
}
