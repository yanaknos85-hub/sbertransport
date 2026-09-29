package issues;

import io.qameta.allure.Feature;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.notifications.NotificationsApplication;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.DefaultNotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = NotificationsApplication.class)
@AutoConfigureMockMvc
@DisplayName("TRANSPORT-12405")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class Transport12405Test {

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DefaultNotificationSettingsRepository defaultNotificationSettingsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Получение настроек")
    void test_getSettings() throws Exception {
        var organizationId = UUID.randomUUID();

        organizationRepository.save(Organization.builder().id(organizationId).build());

        var department = new Department();
        department.setId(UUID.randomUUID());
        department.setOrganizationId(organizationId);

        departmentRepository.save(department);

        var employee = new Employee();
        employee.setId(UUID.fromString(USER_ID));
        employee.setUserId(UUID.fromString(USER_ID));
        employee.setDepartmentId(department.getId());

        employeeRepository.save(employee);
        var expectedList = defaultNotificationSettingsRepository.findAllDefaultSettings()
                .stream().sorted(Comparator.comparing(NotificationSettings::getName)).toList();

        var result = mockMvc.perform(get("/%s/settings/".formatted(organizationId))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedList.size()));

        assertThat(result).isNotNull();

        for (var i = 0; i < expectedList.size(); i++) {
            var expected = expectedList.get(i);

            result
                    .andExpect(jsonPath("$.[%s].id".formatted(i)).value(expected.getId()))
                    .andExpect(jsonPath("$.[%s].channels.length()".formatted(i)).value(expected.getChannels().size()))
                    .andExpect(jsonPath("$.[%s].countings.length()".formatted(i)).value(expected.getCountings().size()))
                    .andExpect(jsonPath("$.[%s].notificationClass".formatted(i)).value(expected.getNotificationClass().name()))
                    .andExpect(jsonPath("$.[%s].organizationId".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].ownerId".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].restrictions".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].timings.length()".formatted(i)).value(expected.getTimings().size()))
                    .andExpect(jsonPath("$.[%s].notificationType".formatted(i)).value(expected.getType().name()))
                    .andExpect(jsonPath("$.[%s].description".formatted(i)).value(expected.getDescription()))
                    .andExpect(jsonPath("$.[%s].name".formatted(i)).value(expected.getName()))
                    ;

            for (var c = 0; c < expected.getChannels().size(); c++) {
                var channel = expected.getChannels().get(c);

                result
                        .andExpect(jsonPath("$.[%s].channels.[%s].channel".formatted(i, c)).value(channel.getChannel().name()))
                        .andExpect(jsonPath("$.[%s].channels.[%s].text".formatted(i, c)).value(channel.getText()))
                        ;
            }

            for (var c = 0; c < expected.getCountings().size(); c++) {
                var countingSettings = expected.getCountings().get(c);

                result
                        .andExpect(jsonPath("$.[%s].countings.[%s].type".formatted(i, c)).value(countingSettings.getType().name()))
                        .andExpect(jsonPath("$.[%s].countings.[%s].count".formatted(i, c)).value(countingSettings.getCount()))
                        .andExpect(jsonPath("$.[%s].countings.[%s].initialPropertyName".formatted(i, c)).value(countingSettings.getInitialPropertyName()))
                        .andExpect(jsonPath("$.[%s].countings.[%s].propertyName".formatted(i, c)).value(countingSettings.getPropertyName()))
                        ;
            }

            for (var t = 0; t < expected.getTimings().size(); t++) {
                var timingSettings = expected.getTimings().get(t);

                result
                        .andExpect(jsonPath("$.[%s].timings.[%s].timeBefore".formatted(i, t)).value(timingSettings.getTimeBefore().toMillis()))
                        .andExpect(jsonPath("$.[%s].timings.[%s].timeFieldName".formatted(i, t)).value(timingSettings.getTimeFieldName()))
                        .andExpect(jsonPath("$.[%s].timings.[%s].eventType".formatted(i, t)).value(new BaseMatcher<String>() {
                            @Override
                            public boolean matches(Object o) {
                                if (o instanceof String value) {
                                    return timingSettings.getType().name().endsWith(value);
                                }
                                return false;
                            }

                            @Override
                            public void describeTo(Description description) {
                                description.appendText(timingSettings.getType().name());
                            }
                        }))
                ;
            }
        }
    }

}
