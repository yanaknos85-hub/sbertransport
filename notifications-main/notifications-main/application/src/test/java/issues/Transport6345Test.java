package issues;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.NotificationsApplication;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = NotificationsApplication.class)
@AutoConfigureMockMvc
@DisplayName("TRANSPORT-6345")
@MockitoBean(types = JwtDecoder.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class Transport6345Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @MockitoBean
    private EmployeeOrganizationFunction checkAccessFunction;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Проверка")
    void test() throws Exception {
        when(checkAccessFunction.apply(any())).thenReturn(UUID.fromString("6e6e04b7-1912-422a-820a-1a6777dfde1b"));

        jdbcTemplate.update("insert into notifications_corporate.employee (id, department_id, user_id, first_name, last_name, patronymic, email, phone, organization_id) values('00000000-0000-0000-0000-000000000000', 'fce3094b-dc70-4cff-9eb5-0343108e6062', '00000000-0000-0000-0000-000000000000', 'Сергей', 'Сергеев', 'Сергеевич', 'test1@sbertransport.ru', '89967235321', 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22');");
        jdbcTemplate.update("INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, parent_type, owner_id, type, text) VALUES ('f78146d7-1e24-4aea-b836-6e0e4aad9f0c', 'LIMIT_DEPARTMENT', 'Уведомление о низком остатке лимита', 'notice_904', '6e6e04b7-1912-422a-820a-1a6777dfde1b', 'ORGANIZATION', null, 'LOW_REMAINS', null);");
        jdbcTemplate.update("INSERT INTO notifications_settings.send_count (id, count, type, property_name, settings_id, initial_property_name) VALUES ('b0aeef52-fbcb-4f35-87df-1fb236311c81', 200, 'EXACT', 'Спасибо', 'f78146d7-1e24-4aea-b836-6e0e4aad9f0c', null);");
        jdbcTemplate.update("INSERT INTO notifications_settings.channel (id, settings_id, channel, text, active) VALUES ('8b4bf422-41a4-4738-a727-61564d941c42', 'f78146d7-1e24-4aea-b836-6e0e4aad9f0c', 'PUSH', 'Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.', true);");
        jdbcTemplate.update("INSERT INTO notifications_settings.channel (id, settings_id, channel, text, active) VALUES ('f5e6dc37-64a6-407e-ac80-61076e2b63c7', 'f78146d7-1e24-4aea-b836-6e0e4aad9f0c', 'SMS', 'Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.', true);");
        jdbcTemplate.update("INSERT INTO notifications_settings.channel (id, settings_id, channel, text, active) VALUES ('02afa29a-63ed-4a0f-bb2e-a5f5d24fb5e0', 'f78146d7-1e24-4aea-b836-6e0e4aad9f0c', 'EMAIL', 'Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.', true);");

        mockMvc.perform(put("/6e6e04b7-1912-422a-820a-1a6777dfde1b/settings/f78146d7-1e24-4aea-b836-6e0e4aad9f0c/")
                        .with(jwt().jwt(builder -> builder.jti("00000000-0000-0000-0000-000000000000")).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "id":"f78146d7-1e24-4aea-b836-6e0e4aad9f0c",
                            "notificationClass":"LIMIT_DEPARTMENT",
                            "notificationType":"LOW_REMAINS",
                            "name":"Уведомление о низком остатке лимита",
                            "restrictions":[
                                {
                                    "type":"ALLOW_ALL",
                                    "roles":[]
                                }
                            ],
                            "description":"notice_904",
                            "countings":[
                                {
                                    "property":"Спасибо",
                                    "type":"EXACT",
                                    "value":800
                                }
                            ],
                            "timings":[
                                {
                                    "timeBefore":30,
                                    "eventType":"AT_EVENT"
                                }
                            ],
                            "channels":[
                                {
                                    "channel":"PUSH",
                                    "text":"Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.",
                                    "enabled":true
                                },
                                {
                                    "channel":"SMS",
                                    "text":"Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.",
                                    "enabled":true
                                },
                                {
                                    "channel":"EMAIL",
                                    "text":"Лимит подразделения израсходован на <процент остатка> %. Обратите внимание на статистику поездок ваших сотрудников.",
                                    "enabled":true
                                }
                            ]}
                        """))
                .andExpect(status().isOk());

        var notificationOpt = notificationSettingsRepository.findById(UUID.fromString("f78146d7-1e24-4aea-b836-6e0e4aad9f0c"));
        assertThat(notificationOpt).isPresent();

        var notification = notificationOpt.get();
        var countings = notification.getCountings();

        assertThat(countings).hasSize(1);
        assertThat(countings.getFirst().getCount()).isEqualTo(800D);
    }

}
