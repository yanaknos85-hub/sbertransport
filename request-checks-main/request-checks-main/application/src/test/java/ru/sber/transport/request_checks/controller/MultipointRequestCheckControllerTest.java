package ru.sber.transport.request_checks.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.request_checks.util.Constants.TIMEZONE_UTC;
import static ru.sber.transport.request_checks.util.ErrorMessages.MULTIPOINT_LIMIT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request_checks.RequestChecksApplication;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.service.TripRequestService;

@SpringBootTest(classes = RequestChecksApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@DisplayName("Проверка MultipointRequestCheckController")
class MultipointRequestCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripRequestService tripRequestService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @Test
    @DisplayName("POST /multipoint/limit - лимит не превышен")
    void testCheckMultipointWithinLimit() throws Exception {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);
        val request = new MultipointCheckRequestDto(passengerId, date, TIMEZONE_UTC);

        doNothing().when(tripRequestService).checkMultipointLimit(any(MultipointCheckRequestDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/multipoint/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.subject("test-user")
                    .claim("roles", "ROLE_USER")))
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /multipoint/limit - лимит превышен (409)")
    void testCheckMultipointLimitExceeded() throws Exception {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);
        val request = new MultipointCheckRequestDto(passengerId, date, TIMEZONE_UTC);

        doThrow(
            new MultipointRequestsLimitExceededException(MULTIPOINT_LIMIT))
            .when(tripRequestService).checkMultipointLimit(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/multipoint/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.subject("test-user")
                    .claim("roles", "ROLE_USER")))
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                jsonPath("$.message").value(MULTIPOINT_LIMIT));
    }

}