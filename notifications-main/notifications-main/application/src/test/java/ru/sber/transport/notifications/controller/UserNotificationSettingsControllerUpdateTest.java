package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.notifications.database.dao.userNotificationSettings.UserNotificationSettingsRepository;
import ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferCommon;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@Feature("app_platform_notifications")
@DisplayName("Проверка контроллера управления уведомлениями пользователя в ЛК для новых настроек")
class UserNotificationSettingsControllerUpdateTest extends GroupTransferCommon {

    private static final String USER_ID = "582a9628-7980-46f0-9264-f7d3699f9e22";

    @Autowired
    private UserNotificationSettingsRepository repository;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
    }

    @Test
    @DisplayName("Проверка контроллера получения уведомлений пользователя в ЛК")
    @Sql(scripts = "/sql/group_transfer_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/employee.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/truncate_settings.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_getForUser() throws Exception {
        String typeRequest = "REQUEST_GROUP_TRANSFER";
        int count = 1;
        int totalCount = 1;

        assertThat(repository.findAll()).isEmpty();

        var result = mockMvc
                .perform(get("/user/settings?page=0&pageSize=16&directionAsc=true&sortField=NAME&class=" + typeRequest)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        result
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalElements").value(totalCount))
                .andExpect(jsonPath("$.numberOfElements").value(count))
                .andExpect(jsonPath("$.content[*].notificationClass").exists())
                .andExpect(jsonPath("$.content[*].notificationClass").value(hasItem(typeRequest)));

        assertThat(repository.findAll()).hasSize(1);

        addSettings();
        totalCount = 3;
        count = 3;

        result = mockMvc
                .perform(get("/user/settings?page=0&pageSize=16&directionAsc=true&sortField=NAME&class=" + typeRequest)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());
        result
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalElements").value(totalCount))
                .andExpect(jsonPath("$.numberOfElements").value(count))
                .andExpect(jsonPath("$.content[*].notificationClass").exists())
                .andExpect(jsonPath("$.content[*].notificationClass").value(hasItem(typeRequest)));

        assertThat(repository.findAll()).hasSize(3);

    }

    private void addSettings() {
        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "На согласовании",
                "notice_4002",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_AWAITING_APPROVAL",
                null,
                "ORGANIZATION",

                "Вам поступила заявка {id} на согласование.",
                true,
                true,
                true,

                0,
                "AT_EVENT",
                null,
                null
        );

        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Согласовано",
                "notice_4003",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_APPROVED",
                null,
                "ORGANIZATION",

                "Ваша заявка {ID} согласована.",
                true,
                true,
                true,

                0,
                "AT_EVENT",
                null,
                null
        );
    }

}

