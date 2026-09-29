package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
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
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.notifications.dto.notification.NotificationSettingsDto;
import ru.sber.transport.notifications.dto.timing.EventType;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.*;
import static ru.sber.transport.notifications.dto.timing.EventType.AT_EVENT;
import static ru.sber.transport.notifications.dto.timing.EventType.DEADLINE;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера тайминга отправки уведомлений")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class NotificationTimingControllerTest {

    private static final String USER_ID = "582a9628-7980-46f0-9264-f7d3699f9e22";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private TimingRepository timingRepository;

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

    private static Stream<Arguments> timingSource() {
        return Stream.of(
                Arguments.of(AT_EVENT, Duration.ofHours(1)),
                Arguments.of(AT_EVENT, Duration.ZERO),
                Arguments.of(DEADLINE, Duration.ofHours(2)),
                Arguments.of(DEADLINE, Duration.ZERO)
        );
    }

    @BeforeEach
    void setup() {
        organizationRepository.save(Organization.builder().id(organizationId).build());

        var department = departmentRepository.save(Department.builder().id(UUID.randomUUID())
                .organizationId(organizationId).build());

        employeeRepository.save(Employee.builder().id(UUID.randomUUID()).userId(UUID.fromString(USER_ID))
                .departmentId(department.getId()).build());
    }

    @Test
    @DisplayName("Проверка на изменение в настройке eventType и timeBefore в Timings и повторное чтение настроек " +
            "TRANSPORT-2027")
    void test_eventType_edit_settings() throws Exception {

        //создали настройку, подставили в нее тайминг, сохранили
        var notificationSettings = new NotificationSettings();
        notificationSettings.setName("Name");
        notificationSettings.setDescription("Description");
        notificationSettings.setParentId(organizationId);
        notificationSettings.setNotificationClass(USER_DELEGATE);
        notificationSettings.setType(NotificationType.ASSIGNMENT);
        notificationSettings = notificationSettingsRepository.save(notificationSettings);

        var timing = new TimingSettings();
        timing.setNotification(notificationSettings);
        timing.setTimeBefore(Duration.ofMinutes(35));
        timing.setType(EventType.AT_EVENT.getModel());

        notificationSettings.getTimings().add(timingRepository.save(timing));
        notificationSettingsRepository.flush();

        var response = mockMvc.perform(get(String.format("/%s/settings/", organizationId))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].timings.[0].timeBefore").value(2100000))
                .andExpect(jsonPath("$[0].timings.[0].eventType").value(EventType.AT_EVENT.name()))
                .andReturn();

        Collection<NotificationSettingsDto> actualNotificationSettings = mapper.readValue(response.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        actualNotificationSettings.iterator().next().getTimings().getFirst().setEventType(EventType.DEADLINE);
        actualNotificationSettings.iterator().next().getTimings().getFirst().setTimeBefore(Duration.ofMinutes(30));
        actualNotificationSettings.iterator().next().setRestriction(null);

        mockMvc.perform(put(String.format("/%s/settings/%s/", organizationId, notificationSettings.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(actualNotificationSettings.iterator().next()))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/%s/settings/", organizationId))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].timings.[0].eventType").value(EventType.DEADLINE.name()))
                .andExpect(jsonPath("$[0].timings.[0].timeBefore").value(1800000));
    }

    @Test
    @DisplayName("Получение данных тайминга. Нет настроек")
    void test_timing_get_noSettings() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        mockMvc.perform(get(String.format("/%s/settings/%s/timing/", organizationId, notification.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Получение данных тайминга. Настройки по-умолчанию")
    void test_timing_get_default() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        var settings = new TimingSettings();
        settings.setNotification(notification);
        settings.setTimeBefore(Duration.ZERO);
        timingRepository.save(settings);

        mockMvc.perform(get(String.format("/%s/settings/%s/timing/", organizationId, notification.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @MethodSource("timingSource")
    @DisplayName("Получение данных тайминга")
    void test_timing_get(EventType eventType, Duration timeBefore) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(USER_DELEGATE);
        notification.setType(NotificationType.ASSIGNMENT);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        var timing = new TimingSettings();
        timing.setNotification(notification);
        timing.setTimeBefore(timeBefore);
        timing.setType(eventType.getModel());
        notification.getTimings().add(timingRepository.save(timing));

        var result = mockMvc.perform(get(String.format("/%s/settings/%s/timing/", organizationId, notification.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());

        result.andExpect(
                        jsonPath("$[0].timeBefore").value(Optional.ofNullable(timeBefore).map(Duration::toMillis).orElse(0L)))
                .andExpect(jsonPath("$[0].eventType").value(eventType.name()));
    }

    @Test
    @DisplayName("Получение тайминга. Уведомлений не существует.")
    void test_timing_notExists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get(String.format("/%s/settings/%s/timing/", organizationId, uuid))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: NotificationSettings, ID:")))
                .andExpect(jsonPath("$.entity.name").value("NotificationSettings"))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }

    @ParameterizedTest
    @MethodSource("timingSource")
    @DisplayName("Установка тайминга")
    void test_restriction_set(EventType eventType, Duration timeBefore) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        var timing = new TimingSettings();
        timing.setTimeBefore(timeBefore);
        timing.setType(eventType.getModel());
        timing.setNotification(notification);
        timingRepository.save(timing);

        mockMvc.perform(put(String.format("/%s/settings/%s/timing/", organizationId, notification.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest(eventType.name(),
                                timeBefore)))
                .andExpect(status().isOk());

        notification = notificationSettingsRepository.findById(notification.getId()).orElseThrow();
        var actual = notification.getTimings().getFirst();
        assertThat(actual.getType()).isEqualTo(eventType.getModel());
        assertThat(actual.getTimeBefore()).isEqualTo(Objects.requireNonNullElse(timeBefore, Duration.ZERO));
    }

    @Test
    @DisplayName("Установка тайминга. Неверный тип события")
    void test_timing_wrongEventType() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification = notificationSettingsRepository.save(notification);

        var result = mockMvc
                .perform(put(String.format("/%s/settings/%s/timing/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest("wrong",
                                Duration.ZERO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("[0].eventType"))
                .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.length()").value(EventType.values().length));

        var i = 0;
        for (var type : CountType.values()) {
            result.andExpect(
                    jsonPath("$.problems[0].constraints[0].value[" + i + "]").value(EventType.values()[i].name()));
        }
    }

    private byte[] createRequest(String eventType, Duration timeBefore) throws JsonProcessingException {
        var dto = new HashMap<String, Object>();
        dto.put("eventType", eventType);
        if (timeBefore != null) {
            dto.put("timeBefore", timeBefore.toMillis());
        }

        return mapper.writeValueAsBytes(List.of(dto));
    }

}