package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.dto.department.SearchEmployeesInfoByDepartmentRequest;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@DisplayName("Проверка контроллера по работе с подразделениями")
public class DepartmentControllerImplTest {
    
    public static final String CONTROLLER_URL = "/department";
    
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @SneakyThrows
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = "/scripts/basic_corp_structure.sql")
    @DisplayName("Проверка поиска сотрудников по отделу и табельному номеру")
    void searchEmployeesInfoByDepartment() {
        var request = new SearchEmployeesInfoByDepartmentRequest("016497", UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"));
        mockMvc.perform(post(CONTROLLER_URL + "/employees")
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[*]").value(Matchers.hasSize(2)))
               .andExpect(jsonPath("$.[*].id", Matchers.containsInAnyOrder(
                       "3cd35c19-fd39-413c-99a0-30f35bd642a8", "7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")))
               .andExpect(jsonPath("$.[*].personnelNumber", Matchers.containsInAnyOrder("2016497", "3016497")))
               .andExpect(jsonPath("$.[*].fullName", Matchers.containsInAnyOrder(
                       "Петров Петр", "Александров Александр Александрович")))
               .andExpect(jsonPath("$.[*].positionName", Matchers.containsInAnyOrder("Планктон", "Планктон")))
               .andExpect(jsonPath("$.[*].tin", Matchers.containsInAnyOrder(null, "123456488880")));
    }
    
    @Test
    @Sql(value = "/scripts/cleanup_database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = "/scripts/basic_corp_structure.sql")
    @DisplayName("Проверка получение списка подразделений с филиалами внутреннего автопарка")
    void getAllWithInternalAutoParkTest() throws Exception {
        mockMvc.perform(get(CONTROLLER_URL)
                            .param("organizationId", "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6")
                            .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.organizationName").value("ЦА"))
           .andExpect(jsonPath("$.departmentWithAutoparkDtoList").value(Matchers.hasSize(1)))
           .andExpect(jsonPath("$.departmentWithAutoparkDtoList.[0].id").value("482e6dcb-03a9-4927-90b4-c7081114a9d8"));
    }
}
