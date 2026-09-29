package ru.sber.transport.telemechanic.controller.impl;

import lombok.SneakyThrows;
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
import ru.sber.transport.telemechanic.exception.EwbContractNotActiveException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@Sql({
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql" })
class OrganizationMedicalLicenseControllerImplTest {
    
    private static final String ROOT_PATH = "/medical-license";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @Test
    @SneakyThrows
    void checkMedicalLicense() {
        AuthorizeUtils.authorize(manager, Role.ROLE_MEDIC.name());
        mockMvc.perform(get(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_MEDIC.name()))))
               .andExpect(status().isNotFound())
               .andExpect(result -> assertInstanceOf(EwbContractNotActiveException.class, result.getResolvedException()))
               .andExpect(result -> assertEquals("Не найден активный договор для организации, id:cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                                 Optional.of(result.getResolvedException()).orElseThrow().getMessage()));
    }
}
