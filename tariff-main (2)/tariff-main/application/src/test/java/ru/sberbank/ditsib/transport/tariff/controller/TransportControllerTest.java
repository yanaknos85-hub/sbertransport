package ru.sberbank.ditsib.transport.tariff.controller;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера тарифов такси")
@ActiveProfiles({"test", "kafka"})
public class TransportControllerTest extends KafkaTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "GUEST", value = "d1c2bb8d-a1b2-480e-8d96-36e9017227c7")
    void testEndpointMapping() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/transport/search")).andReturn();
    }
    
}
