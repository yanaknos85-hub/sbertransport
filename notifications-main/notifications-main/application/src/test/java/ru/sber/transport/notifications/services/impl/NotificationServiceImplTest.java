package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Сервис уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class NotificationServiceImplTest {

    @Autowired
    private NotificationService service;

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Добавление")
    void test_add() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var notificationSettings = new NotificationSettings();
        notificationSettings.setParentId(UUID.randomUUID());
        notificationSettings.setName("Name");
        notificationSettings.setDescription("Description");
        notificationSettings.setOwnerId(employee.getId());
        notificationSettings.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notificationSettings.setType(NotificationType.ALLOCATION);
        var receiver = employeeRepository.save(employee);
        var settings = notificationSettingsRepository.save(notificationSettings);

        var notification = new Notification();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        notification.setEntity(objectMapper.writeValueAsString(new TripApprove(UUID.randomUUID())));
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setSettings(settings);

        var actual = service.save(notification);

        assertThat(actual)
                .matches(act -> act.getId() != null)
                .matches(act -> act.getEntity() != null)
                .matches(act -> act.getReceiverId().equals(employee.getId()))
                .matches(act -> act.getSettings().getType().equals(notificationSettings.getType()))
                .matches(act -> act.getSettings().getParentId().equals(notificationSettings.getParentId()))
                .matches(act -> act.getSettings().getName().equals(notificationSettings.getName()))
                .matches(act -> act.getSettings().getDescription().equals(notificationSettings.getDescription()))
                .matches(act -> act.getSettings().getOwnerId().equals(notificationSettings.getOwnerId()));
    }

    @Test
    @DisplayName("Получение уведомления")
    void test_getOne() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var receiver = employeeRepository.save(employee);

        var notificationSettings = new NotificationSettings();
        notificationSettings.setParentId(UUID.randomUUID());
        notificationSettings.setName("Name");
        notificationSettings.setDescription("Description");
        notificationSettings.setOwnerId(employee.getId());
        notificationSettings.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notificationSettings.setType(NotificationType.ALLOCATION);
        var settings = notificationSettingsRepository.save(notificationSettings);

        var approve = new TripApprove(UUID.randomUUID());
        approve.setApprover(Employee.builder().id(UUID.randomUUID()).build());

        var notification = new Notification();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        notification.setEntity(objectMapper.writeValueAsString(approve));
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setSettings(settings);

        var saved = repository.save(notification);

        var actual = service.get(saved.getId());

        assertThat(actual)
                .matches(act -> act.getReceiverId().equals(saved.getReceiverId()), "Receiver ID")
                .matches(act -> act.getSettings().getId().equals(saved.getSettings().getId()), "Settings ID")
                .matches(act -> {
                            try {
                                return act.getEntityFromJson(TripApprove.class).getApprover().getId().equals(saved.getEntityFromJson(
                                        TripApprove.class).getApprover().getId());
                            } catch (JsonProcessingException e) {
                                log.error("Processing failed", e);
                                return false;
                            }
                        },
                        "Approver ID")
                .matches(act -> {
                            try {
                                return act.getEntityFromJson(TripApprove.class).getRequestId().equals(saved.getEntityFromJson(TripApprove.class).getRequestId());
                            } catch (JsonProcessingException e) {
                                log.error("Processing failed", e);
                                return false;
                            }
                        },
                        "Request ID");
    }

    @Test
    @DisplayName("Удаление уведомления по настройкам")
    void test_deleteAllBySettings() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var receiver = employeeRepository.save(employee);

        var notificationSettings = new NotificationSettings();
        notificationSettings.setParentId(UUID.randomUUID());
        notificationSettings.setName("Name");
        notificationSettings.setDescription("Description");
        notificationSettings.setOwnerId(employee.getId());
        notificationSettings.setNotificationClass(NotificationClass.LIMIT_PERSON);
        notificationSettings.setType(NotificationType.ALLOCATION);
        var settings = notificationSettingsRepository.save(notificationSettings);

        var approve = new TripApprove(UUID.randomUUID());
        approve.setApprover(Employee.builder().id(UUID.randomUUID()).build());

        for (var i = 0; i < 100; i++) {
            var notification = new Notification();
            notification.setEntity(objectMapper.writeValueAsString(approve));
            notification.setReceiverId(receiver.getId());
            notification.setSent(false);
            notification.setSettings(settings);

            repository.save(notification);
        }

        assertThat(repository.count()).isEqualTo(100);

        service.cancelAll(settings);

        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Получение неотправленных уведомлений с таймингом")
    void test_notificationSettings_timing_notSent() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();

        var receiver = employeeRepository.save(employee);
        var count = 100;
        for (var i = 0; i < count; i++) {
            var settings = new NotificationSettings();
            settings.setName("Name " + i);
            settings.setDescription("Description " + i);
            settings.setOwnerId(UUID.randomUUID());
            if (i % 2 == 0) {
                settings.getCountings().addAll(createCountings(settings));
            } else {
                settings.getTimings().addAll(createTimings(settings));
            }
            settings.setNotificationClass(NotificationClass.LIMIT_PERSON);
            settings.setType(NotificationType.ALLOCATION);
            settings.setParentId(UUID.randomUUID());
            settings = notificationSettingsRepository.save(settings);

            var approve = new TripApprove(UUID.randomUUID());
            approve.setApprover(receiver);
            approve.setStatus(true);

            var notification = new Notification();
            notification.setSent(false);
            notification.setSettings(settings);
            notification.setEntity(objectMapper.writeValueAsString(approve));
            notification.setReceiverId(receiver.getId());

            repository.save(notification);
        }

        var actualList = service.getTimingNotSent();

        assertThat(actualList).hasSize(count / 2);
    }

    @Test
    @DisplayName("Получение неотправленных уведомлений с количеством")
    void test_notificationSettings_countings_notSent() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();

        var receiver = employeeRepository.save(employee);
        var count = 100;
        for (var i = 0; i < count; i++) {
            var settings = new NotificationSettings();
            settings.setName("Name " + i);
            settings.setDescription("Description " + i);
            settings.setOwnerId(UUID.randomUUID());
            if (i % 2 == 0) {
                settings.getCountings().addAll(createCountings(settings));
            } else {
                settings.getTimings().addAll(createTimings(settings));
            }
            settings.setNotificationClass(NotificationClass.LIMIT_PERSON);
            settings.setType(NotificationType.ALLOCATION);
            settings.setParentId(UUID.randomUUID());
            settings = notificationSettingsRepository.save(settings);

            var approve = new TripApprove(UUID.randomUUID());
            approve.setApprover(receiver);
            approve.setStatus(true);

            var notification = new Notification();
            notification.setSent(false);
            notification.setSettings(settings);
            notification.setEntity(objectMapper.writeValueAsString(approve));
            notification.setReceiverId(receiver.getId());

            repository.save(notification);
        }

        var actualList = service.getCountingNotSent();

        assertThat(actualList).hasSize(count / 2);
    }

    @Test
    @DisplayName("Получение неотправленных уведомлений")
    void test_notificationSettings_timing_possibleSent() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();

        var receiver = employeeRepository.save(employee);
        var count = 100;
        for (var i = 0; i < count; i++) {
            var settings = new NotificationSettings();
            settings.setName("Name " + i);
            settings.setDescription("Description " + i);
            settings.setOwnerId(UUID.randomUUID());
            settings.getTimings().addAll(createTimings(settings));
            settings.setNotificationClass(NotificationClass.LIMIT_PERSON);
            settings.setType(NotificationType.ALLOCATION);
            settings.setParentId(UUID.randomUUID());
            settings = notificationSettingsRepository.save(settings);

            var approve = new TripApprove(UUID.randomUUID());
            approve.setApprover(receiver);
            approve.setStatus(true);

            var notification = new Notification();
            notification.setSent(i % 2 == 0);
            notification.setSettings(settings);
            notification.setEntity(objectMapper.writeValueAsString(approve));
            notification.setReceiverId(receiver.getId());

            repository.save(notification);
        }

        var actualList = service.getTimingNotSent();

        assertThat(actualList).hasSize(count / 2);
    }

    @Test
    @DisplayName("Получение неотправленных уведомлений")
    void test_notificationSettings_countings_possibleSent() throws JsonProcessingException {
        var employee = Employee.builder().id(UUID.randomUUID()).build();

        var receiver = employeeRepository.save(employee);
        var count = 100;
        for (var i = 0; i < count; i++) {
            var settings = new NotificationSettings();
            settings.setName("Name " + i);
            settings.setDescription("Description " + i);
            settings.setOwnerId(UUID.randomUUID());
            settings.getCountings().addAll(createCountings(settings));
            settings.setNotificationClass(NotificationClass.LIMIT_PERSON);
            settings.setType(NotificationType.ALLOCATION);
            settings.setParentId(UUID.randomUUID());
            settings = notificationSettingsRepository.save(settings);

            var approve = new TripApprove(UUID.randomUUID());
            approve.setStatus(true);

            var notification = new Notification();
            notification.setSent(i % 2 == 0);
            notification.setSettings(settings);
            notification.setEntity(objectMapper.writeValueAsString(approve));
            notification.setReceiverId(receiver.getId());

            repository.save(notification);
        }

        var actualList = service.getCountingNotSent();

        assertThat(actualList).hasSize(count / 2);
    }

    @Test
    @DisplayName("Поиск по идентификатору сущности уведомления")
    void test_findByEntityId() throws JsonProcessingException {
        var entityId = UUID.randomUUID();

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getId), entityId)
                .ignore(Select.field(Trip::getRequests))
                .ignore(Select.field(Trip::getWaypoints))
                .create();

        var settings = Instancio.of(NotificationSettings.class)
                .set(Select.field(NotificationSettings::getChannels), List.of())
                .set(Select.field(NotificationSettings::getCountings), List.of())
                .set(Select.field(NotificationSettings::getTimings), List.of())
                .ignore(Select.field(NotificationSettings::getRestrictions))
                .ignore(Select.field(NotificationSettings::getId))
                .create();

        settings = notificationSettingsRepository.saveAndFlush(settings);

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), UUID.randomUUID())
                .create();

        employeeRepository.saveAndFlush(employee);

        var notification = Instancio.of(Notification.class)
                .set(Select.field(Notification::getEntity), objectMapper.writeValueAsString(trip))
                .set(Select.field(Notification::getEntityId), entityId)
                .set(Select.field(Notification::getSettings), settings)
                .set(Select.field(Notification::getReceiverId), employee.getId())
                .ignore(Select.field(Notification::getId))
                .create();

        repository.save(notification);

        var actualOpt = service.find(entityId);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();
        assertThat(actual.getEntity()).isEqualTo(notification.getEntity());
        assertThat(actual.getEntityId()).isEqualTo(notification.getEntityId());
        assertThat(actual.getReceiverId()).isEqualTo(notification.getReceiverId());
    }

    private List<TimingSettings> createTimings(NotificationSettings notificationSettings) {
        var settings = new TimingSettings();
        settings.setType(EventType.AT_EVENT);
        settings.setTimeBefore(Duration.ZERO);
        settings.setTimeFieldName("fieldName");
        settings.setNotification(notificationSettings);

        return List.of(settings);
    }

    private List<CountingSettings> createCountings(NotificationSettings notificationSettings) {
        var settings = new CountingSettings();

        settings.setCount(10D);
        settings.setType(CountingType.EXACT);
        settings.setInitialPropertyName("initial");
        settings.setPropertyName("property");
        settings.setNotification(notificationSettings);

        return List.of(settings);
    }

}