package ru.sberbank.ditsib.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.BaseIntegrationTest;
import ru.sberbank.ditsib.enumerate.MainLeadStatus;
import ru.sberbank.ditsib.enumerate.TransportType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@DisplayName("Проверка контроллера планирования")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class PlannerControllerTest extends BaseIntegrationTest {

    public static final String CONTROLLER_URL = "/manager/planner";

    @Test
    @Sql(scripts = {"/scripts/basic_corp_structure.sql", "/scripts/main_lead.sql"})
    void getAllRequests() throws Exception {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());

        mockMvc.perform(get(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].mainLeadId").value("8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f"))
                .andExpect(jsonPath("$.[0].transportType").value(TransportType.TAXI.name()))
                .andExpect(jsonPath("$.[0].cost").value(1000))
                .andExpect(jsonPath("$.[0].freeSeats").value(2))
                .andExpect(jsonPath("$.[0].departureTime").value("2025-08-20 19:00:00"))
                .andExpect(jsonPath("$.[0].startPoint").value("Moscow, Russia"))
                .andExpect(jsonPath("$.[0].endPoint").value("Saint Petersburg, Russia"))
                .andExpect(jsonPath("$.[0].status").value(MainLeadStatus.GENERATING.name()));
    }
}