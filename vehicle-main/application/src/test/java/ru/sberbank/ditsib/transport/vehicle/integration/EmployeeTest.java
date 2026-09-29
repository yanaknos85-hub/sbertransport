package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Проверка контроллера сотрдуников")
class EmployeeTest  extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/employee";

    private final static String EMPLOYEE_PES_NUMBER_3 = "3016497";
    private final static String EMPLOYEE_PES_NUMBER_2 = "2016498";
    private final static Long EMPLOYEE_TIN_1 = 123456789098L;

    @BeforeEach
    void resetAuth() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
    }

    @Test
    @SneakyThrows
    @Sql("/scripts/basic_corp_structure.sql")
    void getEmployeeByPersonnelNumber() {
        mockMvc.perform(get(ROOT_PATH)
                .with(jwt()
                        .jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                        .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                .param("personnelNumber", EMPLOYEE_PES_NUMBER_3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70"));

        mockMvc.perform(get(ROOT_PATH)
                .with(jwt()
                        .jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                        .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                .param("personnelNumber", EMPLOYEE_PES_NUMBER_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
