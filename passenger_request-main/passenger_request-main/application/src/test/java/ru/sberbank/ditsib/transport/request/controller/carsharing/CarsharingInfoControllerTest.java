package ru.sberbank.ditsib.transport.request.controller.carsharing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingInfoRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера заявок на получение доп информации о пользователе каршеринга")
@MockitoBean(types = JwtDecoder.class)
class CarsharingInfoControllerTest extends KafkaTest {
    
    private final String USER_ID = "cc2eb25c-b4d0-426b-bf2e-aaaaaaccccc2";
    
    @Autowired
    private MockMvc mvc;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CarsharingInfoRepository carsharingInfoRepository;
    
    @Value("${carsharing.belkaDeepLink:https://belkacar.ru/deeplink}")
    private String carsharingDeepLink;
    
    @Value("${carsharing.sberfriendDeepLink:https://sberfriend.sbrf.ru/sberfriend/#/interaction/new?elementId=12}")
    private String sberfriendDeepLink;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
        employeeRepository.save(
                Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).firstName("test").lastName("test").build());
    }
    
    @AfterEach
    void clear() {
        carsharingInfoRepository.deleteAllInBatch();
    }
    
    @DisplayName("Проверка получения информации")
    @Test
    public void get_test() throws Exception {
        var result = mvc.perform(MockMvcRequestBuilders.get("/carsharing-info/")
                                                       .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var res = mapper.readValue(result.getResponse().getContentAsString(), CarsharingInfoResponseDTO.class);
        assertThat(res.isConsent()).isFalse();
        assertThat(res.isPreviouslyUsed()).isFalse();
        assertThat(res.getDeepLink()).isEqualTo(sberfriendDeepLink);
    }
    
    @DisplayName("Проверка обновления информации")
    @Test
    @WithMockUser(username = USER_ID, roles = "GUEST")
    public void put_test() throws Exception {
        var infoRequest = CarsharingInfoRequestDTO.builder().consent(true).previouslyUsed(true).build();
        var content = mapper.writeValueAsString(infoRequest);
        mvc.perform(MockMvcRequestBuilders.put("/carsharing-info/").content(content)
                                          .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                          .contentType(MediaType.APPLICATION_JSON))
           .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var result = mvc.perform(MockMvcRequestBuilders.get("/carsharing-info/")
                                                       .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var res = mapper.readValue(result.getResponse().getContentAsString(), CarsharingInfoResponseDTO.class);
        assertThat(res.isConsent()).isTrue();
        assertThat(res.isPreviouslyUsed()).isTrue();
        assertThat(res.getDeepLink()).isEqualTo(carsharingDeepLink);
    }
    
}
