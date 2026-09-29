package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.notifications.dto.notification.Channel;
import ru.sber.transport.notifications.dto.notification.NotificationTypeDto;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера настроек уведомлений")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
class NotificationSettingsControllerTest {

    private static final String USER_ID = "582a9628-7980-46f0-9264-f7d3699f9e22";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    private final UUID organizationId = UUID.randomUUID();

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
    }

    private static Stream<Arguments> newNotificationSettingValues() {
        return Stream.of(
                Arguments.of("REQUEST_TAXI", "APPROVE", "Request taxi name", "Request taxi description", "PUSH",
                        "Push text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE", "Request public name", "Request public transport " +
                                "description", "PUSH",
                        "Push text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE", "Request personal name", "Request personal description", "PUSH",
                        "Push text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE", "Request sharing name", "Request car sharing " +
                                "description", "PUSH",
                        "Push text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE", "Request bicycle name", "Request bicycle description",
                        "PUSH",
                        "Push text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE", "Request scooter name", "Request scooter description",
                        "PUSH",
                        "Push text scooter request"),
                Arguments.of("REQUEST_TAXI", "APPROVE_STATUS", "Request taxi name", "Request taxi description", "PUSH",
                        "Push text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE_STATUS", "Request public name",
                        "Request public transport description", "PUSH", "Push text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE_STATUS", "Request personal name",
                        "Request personal description", "PUSH", "Push text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE_STATUS", "Request sharing name", "Request car sharing " +
                                "description", "PUSH",
                        "Push text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE_STATUS", "Request bicycle name",
                        "Request bicycle description", "PUSH", "Push text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE_STATUS", "Request scooter name",
                        "Request scooter description", "PUSH",
                        "Push text scooter request"),
                Arguments.of("REQUEST_LIMIT_PERSON", "APPROVE", "Request personal limit",
                        "Request personal limit description", "PUSH", "Push text person limit request"),
                Arguments.of("REQUEST_LIMIT_DEPARTMENT", "APPROVE", "Request department limit",
                        "Request department limit description", "PUSH", "Push text department limit request",
                        status().isOk()),
                Arguments.of("LIMIT_DEPARTMENT", "ALLOCATION", "Department limit", "Department limit description",
                        "PUSH",
                        "Push text limit department"),
                Arguments.of("LIMIT_PERSON", "ALLOCATION", "Personal limit", "Personal limit description", "PUSH",
                        "Push text person limit"),
                Arguments.of("USER_DELEGATE", "ASSIGNMENT", "Delegate", "Delegate description", "PUSH",
                        "Push text delegate", status().isOk()),
                Arguments.of("USER_OWNER_LIMIT", "ASSIGNMENT", "Owner", "Owner description", "PUSH", "Push text owner",
                        status().isOk()),
                Arguments.of("USER_ASSIGNMENT", "ASSIGNMENT", "Assignment", "Assigment description", "PUSH", "Push text assignment",
                        status().isOk()),
                Arguments.of("REQUEST_TAXI", "APPROVE", "Request taxi name", "Request taxi description", "SMS",
                        "SMS text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE", "Request public name",
                        "Request public transport description", "SMS",
                        "SMS text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE", "Request personal name", "Request personal description",
                        "SMS", "SMS text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE", "Request sharing name",
                        "Request car sharing description", "SMS",
                        "SMS text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE", "Request bicycle name", "Request bicycle description", "SMS",
                        "SMS text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE", "Request scooter name", "Request scooter description", "SMS",
                        "SMS text scooter request"),
                Arguments.of("REQUEST_TAXI", "APPROVE_STATUS", "Request taxi name", "Request taxi description", "SMS",
                        "SMS text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE_STATUS", "Request public name",
                        "Request public transport description", "SMS",
                        "SMS text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE_STATUS", "Request personal name",
                        "Request personal description", "SMS",
                        "SMS text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE_STATUS", "Request sharing name",
                        "Request car sharing description", "SMS",
                        "SMS text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE_STATUS", "Request bicycle name", "Request bicycle description",
                        "SMS", "SMS text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE_STATUS", "Request scooter name", "Request scooter description",
                        "SMS", "SMS text scooter request"),
                Arguments.of("REQUEST_LIMIT_PERSON", "APPROVE", "Request personal limit",
                        "Request personal limit description",
                        "SMS", "SMS text person limit request"),
                Arguments.of("REQUEST_LIMIT_DEPARTMENT", "APPROVE", "Request department limit",
                        "Request department limit description", "SMS", "SMS text department limit request",
                        status().isOk()),
                Arguments.of("LIMIT_DEPARTMENT", "ALLOCATION", "Department limit", "Department limit description",
                        "SMS", "SMS text limit department"),
                Arguments.of("LIMIT_PERSON", "ALLOCATION", "Personal limit", "Personal limit description", "SMS",
                        "SMS text person limit"),
                Arguments.of("USER_DELEGATE", "ASSIGNMENT", "Delegate", "Delegate description", "SMS",
                        "SMS text delegate",
                        status().isOk()),
                Arguments.of("USER_OWNER_LIMIT", "ASSIGNMENT", "Owner", "Owner description", "SMS", "SMS text owner"),
                Arguments.of("USER_ASSIGNMENT", "ASSIGNMENT", "Assignment", "Assigment description", "SMS",
                        "SMS text assignment", status().isOk()),
                Arguments.of("REQUEST_TAXI", "APPROVE", "Request taxi name", "Request taxi description", "EMAIL",
                        "E-mail text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE", "Request public name",
                        "Request public transport description", "EMAIL",
                        "E-mail text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE", "Request personal name", "Request personal description",
                        "EMAIL", "E-mail text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE", "Request sharing name",
                        "Request car sharing description", "EMAIL",
                        "E-mail text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE", "Request bicycle name", "Request bicycle description",
                        "EMAIL", "E-mail text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE", "Request scooter name", "Request scooter description",
                        "EMAIL", "E-mail text scooter request"),
                Arguments.of("REQUEST_TAXI", "APPROVE_STATUS", "Request taxi name", "Request taxi description", "EMAIL",
                        "E-mail text taxi request"),
                Arguments.of("REQUEST_PUBLIC", "APPROVE_STATUS", "Request public name",
                        "Request public transport description", "EMAIL", "E-mail text public request"),
                Arguments.of("REQUEST_PERSONAL", "APPROVE_STATUS", "Request personal name",
                        "Request personal description", "EMAIL", "E-mail text personal request"),
                Arguments.of("REQUEST_CAR_SHARING", "APPROVE_STATUS", "Request sharing name",
                        "Request car sharing description", "EMAIL",
                        "E-mail text car sharing request"),
                Arguments.of("REQUEST_BICYCLE", "APPROVE_STATUS", "Request bicycle name", "Request bicycle description",
                        "EMAIL",
                        "E-mail text bicycle request"),
                Arguments.of("REQUEST_SCOOTER", "APPROVE_STATUS", "Request scooter name", "Request scooter description",
                        "EMAIL",
                        "E-mail text scooter request"),
                Arguments.of("REQUEST_LIMIT_PERSON", "APPROVE", "Request personal limit",
                        "Request personal limit description",
                        "EMAIL", "E-mail text person limit request"),
                Arguments.of("REQUEST_LIMIT_DEPARTMENT", "APPROVE", "Request department limit",
                        "Request department limit description", "EMAIL", "E-mail text department limit request",
                        status().isOk()),
                Arguments.of("LIMIT_DEPARTMENT", "ALLOCATION", "Department limit", "Department limit description",
                        "EMAIL",
                        "E-mail text limit department"),
                Arguments.of("LIMIT_PERSON", "ALLOCATION", "Personal limit", "Personal limit description", "EMAIL",
                        "E-mail text person limit"),
                Arguments.of("USER_DELEGATE", "ASSIGNMENT", "Delegate", "Delegate description", "EMAIL", "E-mail text " +
                                "delegate",
                        status().isOk()),
                Arguments.of("USER_OWNER_LIMIT", "ASSIGNMENT", "Owner", "Owner description", "EMAIL", "E-mail text owner",
                        status().isOk()),
                Arguments.of("USER_ASSIGNMENT", "ASSIGNMENT", "Assignment", "Assigment description", "EMAIL",
                        "E-mail text assignment")
        );
    }

    @BeforeEach
    void setup() {
        organizationRepository.save(Organization.builder().id(organizationId).build());
        var department = departmentRepository.save(Department.builder().id(UUID.randomUUID())
                .organizationId(organizationId).build());
        employeeRepository.save(Employee.builder().id(UUID.randomUUID()).departmentId(department.getId()).userId(UUID.fromString(USER_ID)).build());
    }

    @ParameterizedTest
    @DisplayName("Добавление настроек уведомлений")
    @MethodSource("newNotificationSettingValues")
    void test_add(String notificationClass, String type, String name, String description, String channel, String text)
            throws Exception {
        assertThat(notificationSettingsRepository.count()).isZero();
        var settingsRequest = createSettingsRequest(channel, text);
        var request = createNotificationTypeRequest(notificationClass, type, name, description,
                List.of(settingsRequest));
        var result = mockMvc
                .perform(post(String.format("/%s/settings/", organizationId))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        var database = notificationSettingsRepository.findAll().getFirst();

        result
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value(database.getId().toString()))
                .andExpect(jsonPath("$.notificationType").value(type))
                .andExpect(jsonPath("$.notificationType").value(database.getType().name()))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.description").value(database.getDescription()))
                .andExpect(jsonPath("$.channels.length()").value(1))
                .andExpect(jsonPath("$.channels.length()").value(database.getChannels().size()))
                .andExpect(jsonPath("$.channels[0].channel").value(channel))
                .andExpect(
                        jsonPath("$.channels[0].channel").value(database.getChannels().getFirst().getChannel().name()))
                .andExpect(jsonPath("$.channels[0].text").value(database.getChannels().getFirst().getText()));
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавление массива настроек")
    void test_add_set_of_settings() {
        var requests = new ArrayList<String>();

        for (var i = 0; i < 100; i++) {
            var notificationClass = NotificationClass.values()[i % NotificationClass.values().length].name();
            var type = NotificationType.values()[i % NotificationType.values().length].name();
            var name = "Name " + i;
            var description = "Description " + i;
            var channel = Channel.values()[i % Channel.values().length].name();
            var text = "Text " + i;
            var settings = createSettingsRequest(channel, text);
            requests.add(createNotificationTypeRequest(notificationClass, type, name, description, List.of(settings)));
        }
        mockMvc
                .perform(post(String.format("/%s/settings/mass/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(String.format("[%s]",
                                String.join(",",
                                        requests))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));

        assertThat(notificationSettingsRepository.count()).isEqualTo(100);
    }

    @SneakyThrows
    @Test
    @DisplayName("Изменение массива настроек")
    void test_edit_set_of_settings() {
        var requests = new ArrayList<String>();

        var count = NotificationClass.values().length * NotificationType.values().length;

        for (var i = 0; i < count; i++) {
            var settings = new NotificationSettings();
            settings.setOwnerId(null);
            settings.setDescription("Description " + i);
            settings.setName("Name " + i);
            settings.setNotificationClass(NotificationClass.values()[i % NotificationClass.values().length]);
            settings.setType(NotificationType.values()[i / NotificationClass.values().length]);
            settings.setParentId(organizationId);

            notificationSettingsRepository.save(settings);
        }

        for (var i = 0; i < count; i++) {
            var id = notificationSettingsRepository.findAll().get(i).getId();
            var notificationClass = NotificationClass.values()[i % NotificationClass.values().length].name();
            var type = NotificationType.values()[i / NotificationClass.values().length].name();
            var name = "Edited name " + i;
            var description = "Edited description " + i;
            var channel = Channel.values()[i % Channel.values().length].name();
            var text = "Edited text " + i;
            var settings = createSettingsRequest(channel, text);
            requests.add(createNotificationTypeRequest(id, notificationClass, type, name, description,
                    List.of(settings)));
        }
        mockMvc.perform(put(String.format("/%s/settings/mass/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(String.format("[%s]",
                                String.join(",",
                                        requests))))
                .andExpect(status().isOk());

        assertThat(notificationSettingsRepository.count()).isEqualTo(count);
    }

    @Test
    @DisplayName("Добавление неверного типа уведомления")
    void test_add_wrong_notificationType() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "wrong", "Old name", "description",
                List.of(settingsRequest));
        var result = mockMvc
                .perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("notificationType"))
                .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.length()").value(
                        NotificationTypeDto.values().length));

        var i = 0;
        for (var ignored : CountType.values()) {
            result.andExpect(jsonPath("$.problems[0].constraints[0].value[" + i + "]")
                    .value(NotificationTypeDto
                            .values()[i].name()));
        }
    }

    @Test
    @DisplayName("Добавление неверного типа канала")
    void test_add_wrong_channel() throws Exception {
        var settingsRequest = createSettingsRequest("wrong", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        var result = mockMvc
                .perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].channel"))
                .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.length()").value(Channel.values().length));

        var i = 0;
        for (var ignored : CountType.values()) {
            result.andExpect(jsonPath("$.problems[0].constraints[0].value[" + i + "]")
                    .value(Channel.values()[i].name()));
        }
    }

    @Test
    @DisplayName("Добавление. Нет типа уведомления")
    void test_add_no_notificationType() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", null, "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("notificationType"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    }

    @Test
    @DisplayName("Добавление. Нет названия уведомления")
    void test_add_no_notificationName() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", null, "description",
                List.of(settingsRequest));
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("name"));
    }

    @Test
    @DisplayName("Добавление. Пустое название уведомления")
    void test_add_empty_notificationName() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "     ", "description",
                List.of(settingsRequest));
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").value("     "))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("name"));
    }

    @Test
    @DisplayName("Добавление. Нет канала")
    void test_add_no_channel() throws Exception {
        var settingsRequest = List.of(createSettingsRequest(null, "text"));
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                settingsRequest);
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].channel"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    }

    @Test
    @DisplayName("Добавление. Нет текста")
    void test_add_no_text() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", null);
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].text"));
    }

    @Test
    @DisplayName("Добавление. Пустой текст")
    void test_add_empty_text() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "   ");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").value("   "))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].text"));
    }

    @Test
    @DisplayName("Добавление дубликата")
    void test_add_duplicate() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("LIMIT_DEPARTMENT", "ALLOCATION", "Old name", "Old description",
                List.of(settingsRequest));
        final var fieldMatcher = new BaseMatcher<String>() {
            @Override
            public boolean matches(Object actual) {
                return actual.equals("description") || actual.equals("parent_id");
            }

            @Override
            public void describeTo(Description description) {
            }
        };
        final var descriptionMatcher = new BaseMatcher<String>() {
            @Override
            public boolean matches(Object actual) {
                return actual.equals("Old description") || actual.equals(notification.getParentId().toString());
            }

            @Override
            public void describeTo(Description description) {
            }
        };
        mockMvc.perform(post(String.format("/%s/settings/", organizationId)).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", Matchers.containsString("Conflict data on entity NotificationSettings")))
                .andExpect(jsonPath("$.problems[0].field").value(fieldMatcher))
                .andExpect(jsonPath("$.problems[0].value").value(descriptionMatcher))
                .andExpect(jsonPath("$.problems[1].field").value(fieldMatcher))
                .andExpect(jsonPath("$.problems[1].value").value(descriptionMatcher))
                .andExpect(jsonPath("$.entity.name").value("NotificationSettings"));
    }

    @ParameterizedTest
    @DisplayName("Изменение настроек уведомлений")
    @MethodSource("newNotificationSettingValues")
    void test_edit(String notificationClass, String type, String name, String description, String channel, String text)
            throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest(channel, text);
        var request = createNotificationTypeRequest(notificationClass, type, name, description,
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)).andExpect(status().isOk());
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var database = notificationSettingsRepository.findAll().getFirst();

        assertThat(database.getType()).isEqualTo(notification.getType());
        assertThat(database.getName()).isEqualTo(notification.getName());
        assertThat(database.getDescription()).isEqualTo(notification.getDescription());
        assertThat(database.getId()).isEqualTo(notification.getId());
        assertThat(database.getChannels()).hasSameSizeAs(notification.getChannels());
        assertThat(database.getChannels().getFirst().getChannel())
                .isEqualTo(notification.getChannels().getFirst().getChannel());
        assertThat(database.getChannels().getFirst().getText()).isEqualTo(notification.getChannels().getFirst().getText());
    }

    @Test
    @DisplayName("Изменение типа уведомлений на неверный")
    void test_edit_wrong_notificationType() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "wrong", "Old name", "description",
                List.of(settingsRequest));
        var result =
                mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Bad Request"))
                        .andExpect(jsonPath("$.problems[0].field").value("notificationType"))
                        .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                        .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                        .andExpect(jsonPath("$.problems[0].constraints[0].value.length()").value(
                                NotificationTypeDto
                                        .values().length));

        var i = 0;
        for (var ignored : CountType.values()) {
            result.andExpect(jsonPath("$.problems[0].constraints[0].value[" + i + "]")
                    .value(NotificationTypeDto
                            .values()[i].name()));
        }
    }

    @Test
    @DisplayName("Изменение канала на неверный")
    void test_edit_wrong_channel() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("wrong", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        var result =
                mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Bad Request"))
                        .andExpect(jsonPath("$.problems[0].field").value("channels[0].channel"))
                        .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                        .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                        .andExpect(
                                jsonPath("$.problems[0].constraints[0].value.length()").value(Channel.values().length));

        var i = 0;
        for (var ignored : Channel.values()) {
            result.andExpect(jsonPath("$.problems[0].constraints[0].value[" + i + "]")
                    .value(Channel.values()[i].name()));
        }
    }

    @Test
    @DisplayName("Изменение. Нет типа уведомления")
    void test_edit_no_notificationType() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", null, "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("notificationType"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    }

    @Test
    @DisplayName("Изменение. Нет названия уведомления")
    void test_edit_no_notificationName() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = List.of(createSettingsRequest("PUSH", "text"));
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", null, "description",
                settingsRequest);
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("name"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"));
    }

    @Test
    @DisplayName("Изменение. Пустое название уведомления")
    void test_edit_empty_notificationName() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "     ", "description",
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").value("     "))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("name"));
    }

    @Test
    @DisplayName("Изменение. Нет канала")
    void test_edit_no_channel() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest(null, "text");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].channel"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    }

    @Test
    @DisplayName("Изменение. Нет текста")
    void test_edit_no_text() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", null);
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].text"));
    }

    @Test
    @DisplayName("Изменение. Пустой текст")
    void test_edit_empty_text() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Old description");
        notification.setName("Old name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        assertThat(notificationSettingsRepository.count()).isEqualTo(1);

        var settingsRequest = createSettingsRequest("PUSH", "   ");
        var request = createNotificationTypeRequest("REQUEST_TAXI", "APPROVE", "Old name", "description",
                List.of(settingsRequest));
        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("channels[0].text"))
                .andExpect(jsonPath("$.problems[0].value").value("   "))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"));
    }

    @Test
    @DisplayName("Изменение несуществующего")
    void test_edit_nonExists() throws Exception {
        var settingsRequest = createSettingsRequest("PUSH", "text");
        var request = createNotificationTypeRequest("LIMIT_PERSON", "ALLOCATION", "Old name 2", "description",
                List.of(settingsRequest));
        var uuid = UUID.randomUUID();
        mockMvc.perform(
                        put(String.format("/%s/settings/%s/", organizationId, uuid)).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: NotificationSettings, ID: ")))
                .andExpect(jsonPath("$.entity.name").value("NotificationSettings"))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }

    @Test
    @DisplayName("Удаление")
    void test_delete() throws Exception {
        var notification = new NotificationSettings();
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        var push = new ChannelSettings();
        push.setChannel(ChannelType.EMAIL);
        push.setText("Push");
        push.setNotification(notification);
        push = channelSettingsRepository.save(push);

        var sms = new ChannelSettings();
        sms.setChannel(ChannelType.SMS);
        sms.setText("sms");
        sms.setNotification(notification);
        sms = channelSettingsRepository.save(sms);

        var email = new ChannelSettings();
        email.setChannel(ChannelType.EMAIL);
        email.setText("email");
        email.setNotification(notification);
        email = channelSettingsRepository.save(email);

        notification.getChannels().addAll(List.of(push, sms, email));

        assertThat(notificationSettingsRepository.count()).isEqualTo(1);
        assertThat(channelSettingsRepository.count()).isEqualTo(3);

        mockMvc.perform(delete(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());

        assertThat(notificationSettingsRepository.count()).isZero();
        assertThat(channelSettingsRepository.count()).isZero();
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void test_delete_nonExists() throws Exception {
        var uuid = UUID.randomUUID();
        organizationRepository.deleteAll();
        mockMvc.perform(delete(String.format("/%s/settings/%s/", organizationId, uuid))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: Organization, ID: ")))
                .andExpect(jsonPath("$.entity.name").value("Organization"))
                .andExpect(jsonPath("$.entity.id").value(organizationId.toString()));
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() throws Exception {
        int count = 100;
        for (var i = 0; i < count; i++) {
            var notification = new NotificationSettings();
            notification.setDescription("Description " + i);
            notification.setName("Name " + i);
            notification.setNotificationClass(NotificationClass.values()[i % NotificationClass.values().length]);
            notification.setType(NotificationType.values()[i % NotificationType.values().length]);
            notification.setParentId(organizationId);
            notification = notificationSettingsRepository.save(notification);

            var push = new ChannelSettings();
            push.setChannel(ChannelType.EMAIL);
            push.setNotification(notification);
            push.setText("PUSH");
            push = channelSettingsRepository.save(push);

            var sms = new ChannelSettings();
            sms.setChannel(ChannelType.SMS);
            sms.setNotification(notification);
            sms.setText("SMS");
            sms = channelSettingsRepository.save(sms);

            var email = new ChannelSettings();
            email.setChannel(ChannelType.EMAIL);
            email.setNotification(notification);
            email.setText("Email");
            email = channelSettingsRepository.save(email);

            notification.getChannels().addAll(List.of(push, sms, email));
        }

        var result = mockMvc.perform(get(String.format("/%s/settings/", organizationId))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(count));

        var all = notificationSettingsRepository.findAll(Sort.by("name"));
        for (var i = 0; i < count; i++) {
            var actual = all.get(i);

            result.andExpect(jsonPath("$[" + i + "].id").value(actual.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].notificationType").value(actual.getType().name()))
                    .andExpect(jsonPath("$[" + i + "].notificationClass").value(actual.getNotificationClass().name()))
                    .andExpect(jsonPath("$[" + i + "].name").value(actual.getName()))
                    .andExpect(jsonPath("$[" + i + "].description").value(actual.getDescription()))
                    .andExpect(jsonPath("$[" + i + "].channels.length()").value(actual.getChannels().size()))
                    .andExpect(jsonPath("$[" + i + "].channels[0].channel")
                            .value(actual.getChannels().getFirst().getChannel().name()))
                    .andExpect(jsonPath("$[" + i + "].channels[0].text").value(actual.getChannels().getFirst().getText()));
        }
    }

    @Test
    @DisplayName("Получение одного")
    void test_get() throws Exception {
        var notification = new NotificationSettings();
        notification.setDescription("Description");
        notification.setName("Name");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        var push = new ChannelSettings();
        push.setChannel(ChannelType.PUSH);
        push.setText("push");
        push.setNotification(notification);
        push = channelSettingsRepository.save(push);

        var sms = new ChannelSettings();
        sms.setChannel(ChannelType.SMS);
        sms.setText("sms");
        sms.setNotification(notification);
        sms = channelSettingsRepository.save(sms);

        var email = new ChannelSettings();
        email.setChannel(ChannelType.EMAIL);
        email.setText("email");
        email.setNotification(notification);
        email = channelSettingsRepository.save(email);

        notification.getChannels().addAll(List.of(push, sms, email));

        mockMvc.perform(get(String.format("/%s/settings/%s/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notification.getId().toString()))
                .andExpect(jsonPath("$.notificationType").value(notification.getType().name()))
                .andExpect(jsonPath("$.notificationClass").value(notification.getNotificationClass().name()))
                .andExpect(jsonPath("$.name").value(notification.getName()))
                .andExpect(jsonPath("$.description").value(notification.getDescription()))
                .andExpect(jsonPath("$.channels.length()").value(notification.getChannels().size()))
                .andExpect(
                        jsonPath("$.channels[0].channel").value(notification.getChannels().getFirst().getChannel().name()))
                .andExpect(jsonPath("$.channels[0].text").value(notification.getChannels().getFirst().getText()));
    }

    @Test
    @DisplayName("Получение одного несуществующего")
    void test_get_nonExists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get(String.format("/%s/settings/%s/", organizationId, uuid))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: NotificationSettings, ID: ")))
                .andExpect(jsonPath("$.entity.name").value("NotificationSettings"))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }

    @SneakyThrows(JsonProcessingException.class)
    private String createNotificationTypeRequest(String notificationClass, String type, String name, String description,
                                                 List<Map<String, Object>> settings) {
        var map = new HashMap<String, Object>();
        map.put("notificationClass", notificationClass);
        map.put("notificationType", type);
        map.put("name", name);
        map.put("description", description);
        map.put("channels", settings);

        return mapper.writeValueAsString(map);
    }


    @SneakyThrows(JsonProcessingException.class)
    private String createNotificationTypeRequest(UUID id, String notificationClass, String type, String name,
                                                 String description,
                                                 List<Map<String, Object>> settings) {
        var map = new HashMap<String, Object>();
        map.put("id", id.toString());
        map.put("notificationClass", notificationClass);
        map.put("notificationType", type);
        map.put("name", name);
        map.put("description", description);
        map.put("channels", settings);

        return mapper.writeValueAsString(map);
    }

    private Map<String, Object> createSettingsRequest(String channel, String text) {
        var map = new HashMap<String, Object>();
        map.put("channel", channel);
        map.put("text", text);

        return map;
    }

}