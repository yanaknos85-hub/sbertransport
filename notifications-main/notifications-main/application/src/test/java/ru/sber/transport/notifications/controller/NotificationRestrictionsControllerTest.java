package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.RoleRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionRoles;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.notifications.dto.restriction.RestrictionType;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.notifications.dto.restriction.RestrictionType.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера ограничений отправки уведомлений")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
class NotificationRestrictionsControllerTest {
    
    private static final String USER_ID = "582a9628-7980-46f0-9264-f7d3699f9e22";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private final UUID organizationId = UUID.randomUUID();
    @Autowired
    private JpaRepository<RestrictionSettings, UUID> restrictionsRepository;
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
    
    private static Stream<Arguments> restrictionsSource() {
        return Stream.of(
                Arguments.of(ALLOW_ALL, Collections.emptyList()),
                Arguments.of(ALLOW_ALL, createRoles(3)),
                Arguments.of(ALLOW_BUT, createRoles(4)),
                Arguments.of(DENY_BUT, createRoles(5)),
                Arguments.of(DENY_ALL, createRoles(6)),
                Arguments.of(DENY_ALL, Collections.emptyList())
                        );
    }
    
    private static Collection<Role> createRoles(int count) {
        return IntStream.range(0, count)
                        .mapToObj(NotificationRestrictionsControllerTest::createRole)
                        .collect(Collectors.toList());
    }
    
    private static Role createRole(int i) {
        var role = new Role();
        role.setCode("Code " + i);
        return role;
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
    @DisplayName("Получение списка ограничений. Нет настроек")
    void test_restriction_get_noSettings() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_DEPARTMENT);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
        
        var result = mockMvc.perform(get(String.format("/%s/settings/%s/restrict/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.type").value(ALLOW_ALL.name()))
               .andExpect(jsonPath("$.roles.length()").value(0));

        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Получение списка ограничений. Настройки по-умолчанию")
    void test_restriction_get_default() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);
    
        var result = mockMvc.perform(get(String.format("/%s/settings/%s/restrict/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.type").value(ALLOW_ALL.name()))
               .andExpect(jsonPath("$.roles.length()").value(0));

        assertThat(result).isNotNull();
    }
    
    @ParameterizedTest
    @MethodSource("restrictionsSource")
    @DisplayName("Получение списка ограничений")
    void test_restriction_get(RestrictionType restrictType, List<Role> roles) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.USER_DELEGATE);
        notification.setType(NotificationType.ASSIGNMENT);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.saveAndFlush(notification);
    
        var restriction = new RestrictionSettings();
        restriction.setRestrictType(restrictType.getModel());
        restriction.getRoles().addAll(roles.stream().map(roleRepository::saveAndFlush)
                .map(role -> createRestriction(restriction, role)).toList());
        restriction.setNotification(notification);
        notification.setRestrictions(restriction);
        notificationSettingsRepository.saveAndFlush(notification);
    
        var result = mockMvc.perform(
                get(String.format("/%s/settings/%s/restrict/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.type").value(restrictType.name()));

        assertThat(result).isNotNull();
    
        result = result.andExpect(jsonPath("$.roles.length()").value(roles.size()));
        for (var i = 0; i < roles.size(); i++) {
            result.andExpect(jsonPath("$.roles[" + i + "]").value(roles.get(i).getCode()));
        }
    }
    
    @Test
    @DisplayName("Получение списка ограничений. Уведомлений не существует.")
    void test_restriction_notExists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get(String.format("/%s/settings/%s/restrict/", organizationId, uuid))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message", Matchers.containsString("Data not found: Entity: Notification, ID: ")))
               .andExpect(jsonPath("$.entity.name").value("Notification"))
               .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }
    
    @ParameterizedTest
    @MethodSource("restrictionsSource")
    @DisplayName("Установка ограничений")
    void test_restriction_set(RestrictionType restrictType, List<Role> roles) throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification.setParentId(organizationId);
        notification = notificationSettingsRepository.save(notification);

        roleRepository.saveAll(roles);
    
        mockMvc.perform(put(String.format("/%s/settings/%s/restrict/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest(restrictType.name(),
                                                                                               roles.stream().map(Role::getCode).toList())))
               .andExpect(status().isOk());
    
        var actual = notificationSettingsRepository.findById(notification.getId()).orElseThrow();

        var actualRestricted = actual.getRestrictions().getRoles().stream().map(RestrictionRoles::getRole).toList();

        assertThat(actualRestricted).hasSameElementsAs(roles);
    }
    
    @Test
    @DisplayName("Установка ограничений. Неверный тип ограничения")
    void test_restriction_wrongRestrictType() throws Exception {
        var notification = new NotificationSettings();
        notification.setName("Name");
        notification.setDescription("Description");
        notification.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notification.setType(NotificationType.ALLOCATION);
        notification = notificationSettingsRepository.save(notification);
    
        var result = mockMvc
                .perform(put(String.format("/%s/settings/%s/restrict/", organizationId, notification.getId().toString()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).content(createRequest("wrong",
                                                                                                Collections
                                                                                                        .emptyList())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems[0].field").value("type"))
                .andExpect(jsonPath("$.problems[0].value").value("wrong"))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Enum"))
                .andExpect(
                        jsonPath("$.problems[0].constraints[0].value.length()").value(RestrictionType.values().length));

        assertThat(result).isNotNull();
    
        var i = 0;
        for (var ignored : CountType.values()) {
            result.andExpect(jsonPath("$.problems[0].constraints[0].value[" + i + "]")
                                     .value(RestrictionType.values()[i].name()));
        }
    }
    
    private byte[] createRequest(String restriction, List<String> roles)
            throws JsonProcessingException {
        var dto = new HashMap<String, Object>();
        dto.put("type", restriction);
        dto.put("roles", roles);
        
        return mapper.writeValueAsBytes(dto);
    }
    
    private RestrictionRoles createRestriction(RestrictionSettings restriction, Role role) {
        var restrictionRole = new RestrictionRoles();
        restrictionRole.setRestrictionSettings(restriction);
        restrictionRole.setRole(role);
        return restrictionRole;
    }
    
}