package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
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
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.LIMIT_DEPARTMENT;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationClass.USER_DELEGATE;

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
class NotificationCountingControllerTest {
    
    private static final String USER1_ID = "00000000-0000-0000-0000-000000000000";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Autowired
    private CountingRepository countingRepository;
    
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
    
    private static Stream<Arguments> countingSource() {
        return Stream.of(
                Arguments.of(CountType.EXACT, 10.0, "field"),
                Arguments.of(CountType.PERCENT, 0.2, "field")
                        );
    }
    
    @BeforeEach
    void setup() {
        organizationRepository.save(Organization.builder().id(organizationId).build());
        
        var department = departmentRepository.save(Department.builder().id(UUID.randomUUID())
                                                             .organizationId(organizationId).build());
        
        employeeRepository.save(Employee.builder().id(UUID.randomUUID()).userId(UUID.fromString(USER1_ID))
                                        .departmentId(department.getId()).build());
    }
    
    @Test
    @DisplayName("Получение данных количественного триггера. Нет настроек")
    void test_counting_get_noSettings() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        
        mockMvc.perform(get(String.format("/%s/settings/%s/counting/", organizationId, notification.getId().toString()))
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }
    
    @ParameterizedTest
    @MethodSource("countingSource")
    @DisplayName("Получение данных количественного триггера")
    void test_counting_get(CountType countingType, Double value, String propertyName) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(USER_DELEGATE);
        notification.setType(NotificationType.ASSIGNMENT);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
    
        var counting = new CountingSettings();
        counting.setNotification(notification);
        counting.setCount(value);
        counting.setType(countingType.getModel());
        counting.setPropertyName(propertyName);
        countingRepository.save(counting);
    
        notification.getCountings().add(counting);
    
        mockMvc.perform(get(String.format("/%s/settings/%s/counting/", organizationId, notification.getId().toString()))
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].type").value(countingType.name()))
               .andExpect(jsonPath("$[0].value").value(value))
               .andExpect(jsonPath("$[0].property").value(propertyName));
    }
    
    @Test
    @DisplayName("Получение количественного триггера. Уведомлений не существует.")
    void test_counting_notExists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get(String.format("/%s/settings/%s/counting/", organizationId, uuid))
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: NotificationSettings, ID: ")))
               .andExpect(jsonPath("$.entity.name").value("NotificationSettings"))
               .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }
    
    @ParameterizedTest
    @MethodSource("countingSource")
    @DisplayName("Установка количественного триггера.")
    void test_counting_set(CountType countingType, Double value, String propertyName) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
    
        var counting = new CountingSettings();
        counting.setNotification(notification);
        counting.setCount(value);
        counting.setType(countingType.getModel());
        counting.setPropertyName(propertyName);
        countingRepository.save(counting);
    
        mockMvc.perform(put(String.format("/%s/settings/%s/counting/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                .contentType(MediaType.APPLICATION_JSON).content(createRequest(countingType.name(),
                                                                                               value, propertyName)))
               .andExpect(status().isOk());
    
        notification = notificationSettingsRepository.findById(notification.getId()).orElseThrow();
        var countings = notification.getCountings();
        assertThat(countings).hasSize(1);
        var actual = countings.get(0);
        assertThat(actual.getType()).isEqualTo(countingType.getModel());
        assertThat(actual.getCount()).isEqualTo(value);
        assertThat(actual.getPropertyName()).isEqualTo(propertyName);
    }
    
    @Test
    @DisplayName("Установка тайминга. Неверный тип события")
    void test_counting_wrongEventType() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification = notificationSettingsRepository.save(notification);
    
        var result = mockMvc
                .perform(put(String.format("/%s/settings/%s/counting/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest("wrong",
                                                                                                0.0,
                                                                                                "name")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("[0].type"))
                .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.length()").value(CountType.values().length));
    
        var i = 0;
        for (var ignored : CountType.values()) {
            result.andExpect(
                    jsonPath("$.problems[0].constraints[0].value[" + i + "]").value(CountType.values()[i].name()));
        }
    }
    
    @Test
    @DisplayName("Установка тайминга. Неверные данные")
    void test_counting_wrongData() throws Exception {
        var error = mockMvc
                .perform(put("/%s/settings/%s/counting/".formatted(organizationId, UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest(null,
                                                                                                null,
                                                                                                null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.entity").doesNotExist())
                .andExpect(jsonPath("$.problems").value(new ProblemsMatcher(mapper)));

        assertThat(error).isNotNull();
    }
    
    private byte[] createRequest(String eventType, Double value, String propertyName) throws JsonProcessingException {
        var list = new ArrayList<Map<String, Object>>();
        
        var dto = new HashMap<String, Object>();
        dto.put("type", eventType);
        dto.put("value", value);
        dto.put("property", propertyName);
        
        list.add(dto);
        
        return mapper.writeValueAsBytes(list);
    }

    @RequiredArgsConstructor
    private static class ProblemsMatcher extends BaseMatcher<Object> {

        private final ObjectMapper mapper;

        private final Map<Map<String, Object>, List<String>> invalid = new HashMap<>();

        @SneakyThrows
        @Override
        public boolean matches(Object o) {
            var problems = mapper.readValue(String.valueOf(o), new TypeReference<List<Map<String, Object>>>() {});
            var excludeValue = "null";
            var constraintSize = 1;
            for (var problem : problems) {
                var field = ReflectionUtils.cast(problem.get("field"), String.class);
                var types = List.of("[0].property".equals(field) ? "NotBlank" : "NotNull");
                if (!excludeValue.equals(problem.get("value"))) {
                    appendInvalid(invalid, problem, "field '%s' must not be %s, but it is".formatted(field, excludeValue));
                }
                var contraints = ReflectionUtils.castObjectToList(problem.get("constraints"), List.class);
                if (contraints.size() != constraintSize) {
                    appendInvalid(invalid, problem, "constraints of field '%s' must have size '%s', but it is not".formatted(field, constraintSize));
                } else {
                    for (var i = 0; i < types.size(); i++) {
                        var type = types.get(i);
                        var contraint = ReflectionUtils.castObjectToMap(contraints.get(0), String.class, String.class);
                        if (!type.equals(contraint.get("type"))) {
                            appendInvalid(invalid, problem, "constraint type at index '%s' of field '%s' must have value '%s', but it is not".formatted(i, field, type));
                        }
                        if (contraint.get("value") != null) {
                            appendInvalid(invalid, problem, "constraint value at index '%s' of field '%s' must not have value, but it is".formatted(i, field));
                        }
                    }
                }
            }
            return invalid.isEmpty();
        }

        private void appendInvalid(Map<Map<String, Object>, List<String>> invalid, Map<String, Object> problem, String message) {
            invalid.computeIfAbsent(problem, p -> new ArrayList<>());
            invalid.computeIfPresent(problem, (p, l) -> {
                l.add(message);
                return l;
            });
        }

        @Override
        public void describeMismatch(Object o, Description description) {
            var message = invalid.values().stream().flatMap(Collection::stream).collect(Collectors.joining(System.lineSeparator()));
            description.appendText(message);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Problems matching");
        }
    }
}