package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.authorization.test.AuthorizeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.REQUEST_CARGO;

@UnitTest
@IsolatedTest
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@Feature("app_platform_notifications")
@DisplayName("Проверка контроллера управления уведомлениями пользователя в ЛК ")
class UserNotificationSettingsControllerTest {

    private static final String USER_ID = "582a9628-7980-46f0-9264-f7d3699f9e22";

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

    private static Stream<Arguments> newNotificationSettingValues() {
        return Stream.of(
                Arguments.of("REQUEST_GROUP_TRANSFER", 1, 1),
                Arguments.of("REQUEST_CARGO", 16, 18)
        );
    }

    @ParameterizedTest
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/group_transfer_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/employee.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/truncate_settings.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @MethodSource("newNotificationSettingValues")
    void test_getForUser(String typeRequest, int count, int totalCount) throws Exception {

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

    }

    @Test
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/group_transfer_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/employee.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/truncate_settings.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void search() throws Exception {

        mockMvc
                .perform(get("/user/settings?page=0&pageSize=16&directionAsc=true&sortField=NAME&class=REQUEST_CARGO")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());


        var request = UserNotificationSettingsSearchDto.builder()
                .parentId(UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22"))
                .notificationClass(Set.of(REQUEST_CARGO))
                .build();


        var result = mockMvc
                .perform(post("/user/settings")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        result
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalElements").value(18))
                .andExpect(jsonPath("$.numberOfElements").value(10))
                .andExpect(jsonPath("$.content[*].notificationClass").exists())
                .andExpect(jsonPath("$.content[*].notificationClass").value(hasItem("REQUEST_CARGO"))
                );

    }

    @Test
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/group_transfer_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/employee.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/truncate_settings.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void update() throws Exception {

        var responce = mockMvc
                .perform(get("/user/settings?page=0&pageSize=1&directionAsc=true&sortField=NAME&class=REQUEST_GROUP_TRANSFER")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].notificationClass").value("REQUEST_GROUP_TRANSFER"))
                .andExpect(jsonPath("$.content[0].pushActive").exists())
                .andExpect(jsonPath("$.content[0].pushActive").value(true))
                .andExpect(jsonPath("$.content[0].emailActive").exists())
                .andExpect(jsonPath("$.content[0].emailActive").value(true))
                .andExpect(jsonPath("$.content[0].smsActive").exists())
                .andExpect(jsonPath("$.content[0].smsActive").value(true))
                .andReturn();


        JsonNode actualId = mapper.readTree(responce.getResponse().getContentAsString(StandardCharsets.UTF_8));
        var id = UUID.fromString(actualId.get("content").get(0).get("id").asText());

        var request = new UserNotificationSettingsDto();
        request.setPushActive(false);
        request.setEmailActive(false);
        request.setSmsActive(false);

        mockMvc
                .perform(put("/user/settings/" + id)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pushActive").exists())
                .andExpect(jsonPath("$.pushActive").value(false))
                .andExpect(jsonPath("$.emailActive").exists())
                .andExpect(jsonPath("$.emailActive").value(false))
                .andExpect(jsonPath("$.smsActive").exists())
                .andExpect(jsonPath("$.smsActive").value(false));

        mockMvc
                .perform(get("/user/settings?page=0&pageSize=1&directionAsc=true&sortField=NAME&class=REQUEST_GROUP_TRANSFER")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pushActive").exists())
                .andExpect(jsonPath("$.content[0].pushActive").value(false))
                .andExpect(jsonPath("$.content[0].emailActive").exists())
                .andExpect(jsonPath("$.content[0].emailActive").value(false))
                .andExpect(jsonPath("$.content[0].smsActive").exists())
                .andExpect(jsonPath("$.content[0].smsActive").value(false))
                .andReturn();
    }

}

