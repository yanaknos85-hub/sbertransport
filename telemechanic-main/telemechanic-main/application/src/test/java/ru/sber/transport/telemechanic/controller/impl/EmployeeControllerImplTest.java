package ru.sber.transport.telemechanic.controller.impl;

import lombok.SneakyThrows;
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
import ru.sber.transport.telemechanic.enumerate.Role;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера сотрдуников")
@EmbeddedPostgres
class EmployeeControllerImplTest {
    
    private static final String ROOT_PATH = "/employee";
    public static final UUID EMPLOYEE_ID_1 = UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    private static final UUID EMPLOYEE_ID_3 = UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70");
    @Autowired
    protected MockMvc mockMvc;
    @MockitoBean
    protected AuthorizationManager<?> manager;
    
    @Test
    @SneakyThrows
    @Sql({ "/scripts/cleanup_database.sql",
           "/scripts/basic_corp_structure.sql" })
    void getMedicByFIO() {
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        mockMvc.perform(get(ROOT_PATH)
                                .with(jwt()
                                              .jwt(builder -> builder.jti(EMPLOYEE_ID_1.toString()))
                                              .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .param("fio", "Александр"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[*].id").value(EMPLOYEE_ID_3.toString()));
    }
}
