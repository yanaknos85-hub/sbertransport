package ru.sberbank.ditsib.transport.request.controller.personal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.SharedTest;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;
import ru.sberbank.ditsib.transport.request.service.personal.PersonalTransportRequestSplitCheckService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера проверки заявки на личный транспорт на дробение поездки")
@SpringBootTest(classes = {RequestApplication.class})
@MockitoBean(types = JwtDecoder.class)
public class PersonalTransportRequestCheckControllerTest extends SharedTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonalTransportRequestSplitCheckService personalTransportRequestSplitCheckService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @Test
    @DisplayName("Проверка контроллера валидации заявки на личный транспорт на дробение поездки")
    void splitCheck_whenExpectedCostMoteThanMinAllowedCost_and_shouldCallServiceAndReturn200() throws Exception {
        final var employeeId = UUID.randomUUID();
        final var desiredDate = 1761046303; // в миллисекундах
        final var timeZone = "GMT+3";
        final var expectedDuration = 3600000L; // 1 час в мс
        final var expectedCost = 50000L; // 500.00 руб в копейках

        final var request =
                new PersonalTransportSplitCheckRqDTO(desiredDate, timeZone, employeeId, expectedCost, expectedDuration);

        doNothing().when(personalTransportRequestSplitCheckService).check(Mockito.any(PersonalTransportSplitCheckRqDTO.class));

      final var response = mockMvc.perform(post("/personal-transport/checks/split")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk()).andReturn();

        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}