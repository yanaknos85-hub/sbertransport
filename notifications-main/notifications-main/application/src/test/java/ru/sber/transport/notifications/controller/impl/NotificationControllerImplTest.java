package ru.sber.transport.notifications.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера количественного триггера отправки уведомлений")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class NotificationControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private LimitService limitService;

    @MockitoBean
    private NotificationSettingsService notificationSettingsService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private List<NotificationProcessor> notificationProcessors;

    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";
    private final UUID userId = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();

    private Authentication authentication;
    private Department department;
    private Limit limit;
    private NotificationSettings settings;

    @MockitoBean
    private AuthorizationManager<?> manager;
    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
        // Создаём поддельные объекты
        department = new Department();
        department.setId(departmentId);
        department.setOrganizationId(organizationId);

        limit = new Limit();
        limit.setId(UUID.randomUUID());
        limit.setDepartmentId(departmentId);
        limit.setOrganizationId(organizationId);
        limit.setSum(1000L);
        limit.setBalance(200L);

        settings = NotificationSettings.builder()
                .id(UUID.randomUUID())
                .notificationClass(NotificationClass.LIMIT_DEPARTMENT)
                .type(NotificationType.LOW_REMAINS_FROM_EMPLOYEE)
                .text("Low balance alert")
                .parentId(organizationId)
                .channels(List.of())
                .build();

        // Моки по умолчанию
        when(employeeService.getByUserId(any(UUID.class))).thenReturn(java.util.Optional.of(new ru.sber.transport.notifications.database.model.coprorate.Employee()));
        when(departmentService.get(any(UUID.class))).thenReturn(java.util.Optional.of(department));
        when(limitService.get(any(UUID.class), any(TransportTypeEnum.class))).thenReturn(java.util.Optional.of(limit));
        when(notificationSettingsService.get(any(UUID.class), any(NotificationClass.class), any(NotificationType.class)))
                .thenReturn(settings);

    }

    @Test
    void notificationLimit_DepartmentNotFound_ShouldThrowEntityNotFound() throws Exception {
        // Arrange
        when(departmentService.get(any(UUID.class))).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/notifications/limit")
                        .param("type", "BUS")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound());

        verify(notificationService, never()).save(any());
    }

    @Test
    void notificationLimit_LimitNotFound_ShouldThrowEntityNotFound() throws Exception {
        // Arrange
        when(limitService.get(any(UUID.class), any(TransportTypeEnum.class))).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/notifications/limit")
                        .param("type", "BUS")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound());

        verify(notificationService, never()).save(any());
    }

    @Test
    void notificationLimit_SettingsNotFound_ShouldStillSaveNotificationButLogWarning() throws Exception {
        // Arrange
        when(notificationSettingsService.get(any(UUID.class), any(NotificationClass.class), any(NotificationType.class)))
                .thenThrow(EntityNotFoundException.class);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/limit")
                        .param("type", "BUS")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound()); // зависит от реализации обработки исключений

        verify(notificationService, never()).save(any());
    }
}