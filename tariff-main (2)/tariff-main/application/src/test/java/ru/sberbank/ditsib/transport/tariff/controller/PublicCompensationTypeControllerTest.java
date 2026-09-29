package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sber.transport.messaging.kafka.test.KafkaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера типов компенсации за общественный транспорт")
@ActiveProfiles({"test", "kafka"})
class PublicCompensationTypeControllerTest extends KafkaTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Проверка получения всех типов компенсации")
    void getAll_success() throws Exception {
        var response = mockMvc.perform(get("/public/compensation/type"))
                              .andExpect(status().isOk());
        
        var actualList = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                                new TypeReference<List<PublicCompensationType>>() {
                                                });
        
        assertThat(actualList.size()).isEqualTo(4);
        assertThat(actualList.contains(PublicCompensationType.CITY_TRIP_COMPENSATION)).isTrue();
        assertThat(actualList.contains(PublicCompensationType.SUBURB_TRIP_COMPENSATION)).isTrue();
        assertThat(actualList.contains(PublicCompensationType.TRAVEL_CARD_COMPENSATION)).isTrue();
        assertThat(actualList.contains(PublicCompensationType.PAID_SERVICES_COMPENSATION)).isTrue();
    }
}