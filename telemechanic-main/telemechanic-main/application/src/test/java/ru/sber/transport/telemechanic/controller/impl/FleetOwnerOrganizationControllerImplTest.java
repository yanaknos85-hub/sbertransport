package ru.sber.transport.telemechanic.controller.impl;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
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
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;

@DisplayName("Проверка контроллера по работе с организациями владельцев автопарков")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class FleetOwnerOrganizationControllerImplTest {
    
    public static final String CONTROLLER_URL = "/fleet-owner-organizations";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @SneakyThrows
    @Test
    @Sql({ "/scripts/basic_corp_structure.sql", "/scripts/fleet_owner_organization.sql" })
    void getFleetOwnerOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var expected1 = new GetAllActiveOrganizationNamesDto(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"), "Тест2");
        var expected2 = new GetAllActiveOrganizationNamesDto(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"), "ЦА");
        var expected3 = new GetAllActiveOrganizationNamesDto(UUID.fromString("11501599-b498-4e9c-8b75-3e5899c445c0"), "Тест3");
        mockMvc.perform(get(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[*].id", Matchers.containsInAnyOrder(
                       expected1.id().toString(), expected2.id().toString(), expected3.id().toString())))
               .andExpect(jsonPath("$.[*].name", Matchers.containsInAnyOrder(
                       expected1.name(), expected2.name(), expected3.name())));
    }
}