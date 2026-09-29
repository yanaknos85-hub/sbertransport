package ru.sber.transport.notifications.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.services.CountingService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=false"} )
@DisplayName("Проверка сервиса всех настройок уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
public class NotificationGlobalProcessorTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CountingRepository countingRepository;

    @Autowired
    private TimingRepository  timingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationSettingsRepository settingsRepository;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    @Autowired
    private NotificationSettingsService notificationSettingsService;

    @Autowired
    private CountingService countingService;

    @Autowired
    private TimingService timingService;

    @MockitoBean
    private SmsSender smsSender;

    @Autowired
    private NotificationGlobalProcessor<TripRequest> notificationGlobalProcessor;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID departmentHeadId = UUID.randomUUID();
    private final NotificationType notificationType = NotificationType.APPROVE_STATUS;
    private final UUID passenger = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {


            var settings = new NotificationSettings();
//            settings.setOwnerId(departmentHeadId);
            settings.setDescription("Description");
            settings.setName("Name");
            settings.setNotificationClass(NotificationClass.REQUEST_TAXI);
            settings.setType(NotificationType.APPROVE_STATUS);
            settings.setParentId(organizationId);
            settings = settingsRepository.save(settings);


            var channelSettings = new ChannelSettings();
            channelSettings.setActive(true);
            channelSettings.setText("Text");
            channelSettings.setChannel(ChannelType.SMS);
            channelSettings.setNotification(settings);

            channelSettingsRepository.save(channelSettings);

            var channelSettingsOld = new ChannelSettings();
            channelSettingsOld.setActive(false);
            channelSettingsOld.setText("Text");
            channelSettingsOld.setChannel(ChannelType.SMS);
            channelSettingsOld.setNotification(settings);

            channelSettingsRepository.save(channelSettingsOld);

            var countingSettings = new CountingSettings();
            countingSettings.setInitialPropertyName(null);
            countingSettings.setPropertyName("Спасибо");
            countingSettings.setType(CountingType.EXACT);
            countingSettings.setCount(200.);
            countingSettings.setNotification(settings);
            countingRepository.save(countingSettings);

            var timingSettings = new TimingSettings();
            timingSettings.setTimeBefore(Duration.ZERO);
            timingSettings.setDeadlineFieldName("dealine");
            timingSettings.setType(EventType.AT_EVENT);
            timingSettings.setNotification(settings);
            timingRepository.save(timingSettings);

            Employee employee = new Employee();
            employee.setId(passenger);
            employee.setPhoneConfirmed(true);
            employee.setPhone("+79111111111");
            employee.setOrganizationId(organizationId);
            employee.setDepartmentId(departmentHeadId);
            employeeRepository.save(employee);

        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            settingsRepository.deleteAll();
            countingRepository.deleteAll();
            employeeRepository.deleteAll();
            timingRepository.deleteAll();
            channelSettingsRepository.deleteAll();
        });
    }

    @Test
    @DisplayName("Получение")
    void test_getBySettings() throws JsonProcessingException {

        var newSettingsList = settingsRepository.findByParentIdAndNotificationClassAndTypeAndOwnerId(organizationId, NotificationClass.REQUEST_TAXI, notificationType, null);

//        NotificationSettings settings = settingsRepository.findAll().get(0);
        NotificationSettings settings = notificationSettingsService.get(organizationId,
                NotificationClass.REQUEST_TAXI,
                notificationType);

        var actualList = countingService.getOfSettings(settings);
        assertThat(actualList).hasSize(1);

        var actualIimingList = timingService.getOfSettings(settings);
        assertThat(actualIimingList).hasSize(1);

        TripRequest approve = new TripRequest();

        notificationGlobalProcessor.process(passenger, settings, approve);

        verify(smsSender, times(1)).send(any(), any(), any());
    }
}
