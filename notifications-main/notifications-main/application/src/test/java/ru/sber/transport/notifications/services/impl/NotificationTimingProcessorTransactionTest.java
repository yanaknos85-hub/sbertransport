package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.collections.MapUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@EmbeddedPostgres
@SpringBootTest(properties = {"grpc.server.port=-1",
        "spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=false"})
@DisplayName("Интеграционный тест транзакций NotificationTimingProcessor")
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationTimingProcessorTransactionTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TimingService timingService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationSender notificationSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MapUtils mapUtils;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    @Autowired
    private TimingRepository timingRepository;

    @Autowired
    private CountingRepository countingRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private NotificationTimingProcessorImpl processor;

    @SpyBean
    private NotificationRepository spyNotificationRepository;

    private final UUID organizationId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");
    private UUID settingsId;
    private UUID employee1Id;
    private UUID employee2Id;
    private UUID employee3Id;
    private UUID notification1Id;
    private UUID notification2Id;
    private UUID notification3Id;

    @BeforeEach
    @Transactional
    void setup() throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());

        // Создаем настройку уведомления
        var settings = new NotificationSettings();
        settings.setDescription("notice_test");
        settings.setName("Test Notification");
        settings.setNotificationClass(NotificationClass.LIMIT_PERSON);
        settings.setType(NotificationType.APPROVE);
        settings.setParentType(NotificationSettings.ParentType.ORGANIZATION);
        settings.setParentId(organizationId);
        settings = notificationSettingsRepository.save(settings);
        settingsId = settings.getId();

        // Создаем channel settings
        var channelSettings = new ChannelSettings();
        channelSettings.setActive(true);
        channelSettings.setText("Test message");
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setNotification(settings);
        channelSettingsRepository.save(channelSettings);

        // Создаем timing settings
        var timingSettings = new TimingSettings();
        timingSettings.setTimeBefore(Duration.ZERO);
        timingSettings.setDeadlineFieldName(null);
        timingSettings.setTimeFieldName(null);
        timingSettings.setType(EventType.AT_EVENT);
        timingSettings.setNotification(settings);
        timingRepository.save(timingSettings);

        // Создаем первого сотрудника
        var employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .email("employee1@test.com")
                .firstName("Employee")
                .lastName("One")
                .build();
        employee1Id = employeeRepository.save(employee1).getId();

        // Создаем request и tripApprove для первого сотрудника (успешный кейс)
        var request1 = new TripRequest();
        request1.setId(UUID.randomUUID());
        request1.setPassengerId(employee1Id);
        request1.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request1.setPassengerCount(1);

        var tripApprove1 = new TripApprove();
        tripApprove1.setRequest(request1);
        tripApprove1.setApprover(employee1);
        tripApprove1.setStatus(true);

        // Создаем notification 1 (должен быть помечен как sent)
        var notification1 = new Notification();
        notification1.setReceiverId(employee1Id);
        notification1.setEntity(objectMapper.writeValueAsString(tripApprove1));
        notification1.setSettings(settings);
        notification1.setSent(false);
        notification1Id = notificationRepository.save(notification1).getId();

        // Создаем второго сотрудника
        var employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .email("employee2@test.com")
                .firstName("Employee")
                .lastName("Two")
                .build();
        employee2Id = employeeRepository.save(employee2).getId();

        // Создаем request и tripApprove для второго сотрудника (кейс с ошибкой)
        var request2 = new TripRequest();
        request2.setId(UUID.randomUUID());
        request2.setPassengerId(employee2Id);
        request2.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        var tripApprove2 = new TripApprove();
        tripApprove2.setRequest(request2);
        tripApprove2.setApprover(employee2);
        tripApprove2.setStatus(true);

        // Создаем notification 2 (с несуществующим receiverId - будет ошибка в getReceiver)
        // После изменения кода этот notification будет обрабатываться в новой транзакции
        // и помечаться как sentError=true, но первая транзакция не пострадает
        var notification2 = new Notification();
        notification2.setReceiverId(employee2Id);
        notification2.setEntity(objectMapper.writeValueAsString(tripApprove2));
        notification2.setSettings(settings);
        notification2.setSent(false);
        notification2 = notificationRepository.save(notification2);
        notification2Id = notification2.getId();

        // Создаем третьего сотрудника
        var employee3 = Employee.builder()
                .id(UUID.randomUUID())
                .email("employee3@test.com")
                .firstName("Employee")
                .lastName("Three")
                .build();
        employee3Id = employeeRepository.save(employee3).getId();

        // Создаем request и tripApprove для третьего сотрудника (еще один успешный кейс)
        var request3 = new TripRequest();
        request3.setId(UUID.randomUUID());
        request3.setPassengerId(employee3Id);
        request3.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        var tripApprove3 = new TripApprove();
        tripApprove3.setRequest(request3);
        tripApprove3.setApprover(employee3);
        tripApprove3.setStatus(true);

        // Создаем notification 3 (еще один успешный - должен быть sent=true, sentError=false)
        var notification3 = new Notification();
        notification3.setReceiverId(employee3Id);
        notification3.setEntity(objectMapper.writeValueAsString(tripApprove3));
        notification3.setSettings(settings);
        notification3.setSent(false);
        notification3 = notificationRepository.save(notification3);
        notification3Id = notification3.getId();
    }

    @Test
    @DisplayName("Три notification: первый и третий успешные (sent=true, sentError=false), второй с ошибкой (sentError=true, но не откат первого и третьего)")
    void test_three_notifications_different_transactions() throws JsonProcessingException {
        // given: Проверяем начальное состояние
        var savedNotification1Before = notificationRepository.findById(notification1Id).orElseThrow();
        var savedNotification2Before = notificationRepository.findById(notification2Id).orElseThrow();
        assertThat(savedNotification1Before.isSent()).isFalse();
        assertThat(savedNotification2Before.isSent()).isFalse();

        var count = new AtomicInteger(0);

        doThrow(new IllegalArgumentException("TEST"))
                .when(spyNotificationRepository)
                .save(argThat(n -> notification2Id.toString().equals(n.getId().toString())
                        && count.incrementAndGet() == 1));

        // when: Запускаем обработку всех notification
        processor.process();

        transactionTemplate.executeWithoutResult(s -> {
            // then: Проверяем notification 1 - должен быть отправлен (успешная транзакция)
            var savedNotification1After = notificationRepository.findById(notification1Id).orElseThrow();
            assertThat(savedNotification1After.isSent()).as("Notification 1 должен быть sent=true после успешной обработки").isTrue();
            assertThat(savedNotification1After.isSentError()).isFalse();

            // then: Проверяем notification 2 - должен быть помечен как error, но НЕ откачен из БД
            // (т.к. REQUIRES_NEW создает новую транзакцию, и ошибка не влияет на первую)
            var savedNotification2After = notificationRepository.findById(notification2Id).orElseThrow();
            assertThat(savedNotification2After.isSentError()).as("Notification 2 должен быть sentError=true при ошибке обработки").isTrue();

            // then: Проверяем notification 3 - должен быть успешно отправлен (sent=true, sentError=false)
            var savedNotification3After = notificationRepository.findById(notification3Id).orElseThrow();
            assertThat(savedNotification3After.isSent()).as("Notification 3 должен быть sent=true после успешной обработки").isTrue();
            assertThat(savedNotification3After.isSentError()).as("Notification 3 не должен иметь sentError=true").isFalse();
        });
    }

    @AfterEach
    @Transactional
    void tearDown() {
        notificationSettingsRepository.deleteAll();
        countingRepository.deleteAll();
        channelSettingsRepository.deleteAll();
        timingRepository.deleteAll();
        employeeRepository.deleteAll();
        notificationRepository.deleteAll();
    }
}
