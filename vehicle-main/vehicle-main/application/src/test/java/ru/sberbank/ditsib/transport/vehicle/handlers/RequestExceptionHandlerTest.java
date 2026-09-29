package ru.sberbank.ditsib.transport.vehicle.handlers;

import ch.qos.logback.classic.Level;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.LoggingExtension;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_CORP_CLIENT;

@DisplayName("Обрабокта rest исключений")
@SpringBootTest(properties = "logging.level.ru.sberbank.ditsib=DEBUG")
@EmbeddedPostgres
@AutoConfigureMockMvc
class RequestExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private TransportService transportService;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(RequestExceptionHandler.class);
    
    @SneakyThrows
    @Test
    void handleTaskRejectedException() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doThrow(new TaskRejectedException("text")).when(transportService).getIndicatorsDateInfo(any(UUID.class), any(UUID.class));
        mockMvc.perform(get("/indicators/" + UUID.randomUUID())
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isTooEarly())
               .andExpect(result -> assertInstanceOf(TaskRejectedException.class, result.getResolvedException()))
               .andExpect(result -> assertEquals("text", Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Попробуйте снова через несколько минут", firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("text", secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }
}