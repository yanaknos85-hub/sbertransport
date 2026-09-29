package ru.sberbank.ditsib.transport.reports.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.service.RecalculateService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Проверка контроллера работы с платежками")
@Import({ObjectMapper.class, JacksonAutoConfiguration.class})
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
class PaymentControllerImplTest extends KafkaTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private RecalculateService recalculateService;
    
    @MockitoBean
    private AuthorizationManager<?> authorizationManager;
    
    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(authorizationManager);
    }
    
    @Test
    @DisplayName("Перерасчет")
    @WithMockUser(roles = "ADMIN_DATA_MASTER")
    void test_recalculate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payment/recalculate?" +
                                                   "statuses=PERSONAL_ORDER_PAYMENT_FORMATION" +
                                                   "&statuses=PERSONAL_PAYMENT_AWAITING" +
                                                   "&from=2023-01-01T00:00:00.000" +
                                                   "&to=2023-01-02T00:00:00.000" +
                                                   "&authorId=00000000-0000-0000-0000-000000000000" +
                                                   "&authorId=00000000-0000-0000-0000-000000000001")
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                       )
               .andExpect(status().isOk());
        
        var statusesCaptor = ArgumentCaptor.forClass(List.class);
        var authorCaptor = ArgumentCaptor.forClass(List.class);
        var fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        var toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(recalculateService).recalculate(statusesCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), authorCaptor.capture());
        
        assertThat(statusesCaptor.getValue()).isNotNull().hasSameElementsAs(List.of(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                                                                                    TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
        assertThat(fromCaptor.getValue()).isNotNull().isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
        assertThat(toCaptor.getValue()).isNotNull().isEqualTo(LocalDateTime.of(2023, 1, 2, 0, 0, 0));
        assertThat(authorCaptor.getValue()).isNotNull().hasSameElementsAs(List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"),
                                                                                    UUID.fromString("00000000-0000-0000-0000-000000000001")));
    }
    
    @Test
    @DisplayName("Перерасчет. Нет автора")
    @WithMockUser(roles = "ADMIN_DATA_MASTER")
    void test_recalculate_noAuthor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payment/recalculate?" +
                                                   "statuses=PERSONAL_ORDER_PAYMENT_FORMATION" +
                                                   "&statuses=PERSONAL_PAYMENT_AWAITING" +
                                                   "&from=2023-01-01T00:00:00.000" +
                                                   "&to=2023-01-02T00:00:00.000"))
               .andExpect(status().isOk());
    
        var statusesCaptor = ArgumentCaptor.forClass(List.class);
        var authorCaptor = ArgumentCaptor.forClass(List.class);
        var fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        var toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(recalculateService).recalculate(statusesCaptor.capture(), fromCaptor.capture(), toCaptor.capture(), authorCaptor.capture());
    
        assertThat(statusesCaptor.getValue()).isNotNull().hasSameElementsAs(List.of(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                                                                                    TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
        assertThat(fromCaptor.getValue()).isNotNull().isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
        assertThat(toCaptor.getValue()).isNotNull().isEqualTo(LocalDateTime.of(2023, 1, 2, 0, 0, 0));
        assertThat(authorCaptor.getValue()).isNull();
    }
    
    @Test
    @DisplayName("Перерасчет. Неверный интервал")
    void test_recalculate_wrongInterval() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payment/recalculate?" +
                                                   "statuses=PERSONAL_ORDER_PAYMENT_FORMATION" +
                                                   "&statuses=PERSONAL_PAYMENT_AWAITING" +
                                                   "&to=2023-01-01T00:00:00.000&from=2023-01-02T00:00:00.000" +
                                                   "&authorId=00000000-0000-0000-0000-000000000000" +
                                                   "&authorId=00000000-0000-0000-0000-000000000001")
                                              .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
               .andExpectAll(
                       status().isBadRequest(),
                       jsonPath("$.path").value("/payment/recalculate"),
                       jsonPath("$.message").value("Type mismatch"),
                       jsonPath("$.problems.length()").value(2),
                       jsonPath("$.problems.[0].field").value("from"),
                       jsonPath("$.problems.[0].value").value("2023-01-02T00:00:00"),
                       jsonPath("$.problems.[0].constraints.length()").value(1),
                       jsonPath("$.problems.[0].constraints.[0].type").value("NotAfter"),
                       jsonPath("$.problems.[0].constraints.[0].value").value("2023-01-01T00:00:00"),
                       jsonPath("$.problems.[1].field").value("to"),
                       jsonPath("$.problems.[1].value").value("2023-01-01T00:00:00"),
                       jsonPath("$.problems.[1].constraints.length()").value(1),
                       jsonPath("$.problems.[1].constraints.[0].type").value("NotBefore"),
                       jsonPath("$.problems.[1].constraints.[0].value").value("2023-01-02T00:00:00")
                            );
    }
    
    @Test
    @DisplayName("Перерасчет. Нет дат")
    void test_recalculate_noDates() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payment/recalculate?" +
                                                   "statuses=PERSONAL_ORDER_PAYMENT_FORMATION" +
                                                   "&statuses=PERSONAL_PAYMENT_AWAITING" +
                                                   "&authorId=00000000-0000-0000-0000-000000000000" +
                                                   "&authorId=00000000-0000-0000-0000-000000000001")
                                              .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
               .andExpectAll(
                       status().isBadRequest(),
                       jsonPath("$.path").value("/payment/recalculate"),
                       jsonPath("$.message").value("Type mismatch"),
                       jsonPath("$.problems.length()").value(2),
                       jsonPath("$.problems.[0].field").value("from"),
                       jsonPath("$.problems.[0].value").value("null"),
                       jsonPath("$.problems.[0].constraints.length()").value(1),
                       jsonPath("$.problems.[0].constraints.[0].type").value("NotNull"),
                       jsonPath("$.problems.[1].field").value("to"),
                       jsonPath("$.problems.[1].value").value("null"),
                       jsonPath("$.problems.[1].constraints.length()").value(1),
                       jsonPath("$.problems.[1].constraints.[0].type").value("NotNull")
                            );
    }
    
    @Test
    @DisplayName("Перерасчет. Нет статусов")
    void test_recalculate_noStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payment/recalculate?" +
                                                   "from=2023-01-01T00:00:00.000&to=2023-01-02T00:00:00.000&" +
                                                   "authorId=00000000-0000-0000-0000-000000000000&" +
                                                   "&authorId=00000000-0000-0000-0000-000000000001")
                                              .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
               .andExpectAll(
                       status().isBadRequest(),
                       jsonPath("$.path").value("/payment/recalculate"),
                       jsonPath("$.message").value("Type mismatch"),
                       jsonPath("$.problems.length()").value(1),
                       jsonPath("$.problems.[0].field").value("statuses"),
                       jsonPath("$.problems.[0].value").value("[]"),
                       jsonPath("$.problems.[0].constraints.length()").value(1),
                       jsonPath("$.problems.[0].constraints.[0].type").value("NotEmpty")
                            );
    }
    
}