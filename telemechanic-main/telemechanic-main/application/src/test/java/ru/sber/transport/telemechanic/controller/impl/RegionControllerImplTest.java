package ru.sber.transport.telemechanic.controller.impl;

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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;

@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка контроллера словарей")
class RegionControllerImplTest {

    public static final String CONTROLLER_URL = "/region-codes";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    @Sql("/scripts/region.sql")
    @DisplayName("Получение списка регионов")
    void getRegions() {
        mockMvc.perform(get(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]").value(Matchers.hasSize(3)))
                .andExpect(jsonPath("$.[*].code", Matchers.containsInAnyOrder("01", "02", "03")))
                .andExpect(jsonPath("$.[*].title", Matchers.containsInAnyOrder(
                        "Первая область (01)",
                        "Вторая область (02)",
                        "Третья область (03)")));
    }
}
