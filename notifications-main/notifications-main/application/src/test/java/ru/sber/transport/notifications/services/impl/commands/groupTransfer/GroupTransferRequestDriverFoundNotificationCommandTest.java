package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

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
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.Address;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.request.Waypoint;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=false"} )
@DisplayName("Проверка сервиса всех настройок уведомлений GroupTransferDriverFound")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
class GroupTransferRequestDriverFoundNotificationCommandTest extends GroupTransferCommon {
    public static final String CUSTOMER_PHONE_FIELD = "addContactPhone";
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CountingRepository countingRepository;

    @Autowired
    private TimingRepository timingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationSettingsRepository settingsRepository;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    @Autowired
    private NotificationSettingsService notificationSettingsService;

    @Autowired
    private TimingService timingService;

    @MockitoBean
    private SmsSender smsSender;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private GroupTransferRequestDriverFoundNotificationCommand command;


    @Autowired
    private NotificationRepository notificationRepository;

    private final UUID organizationId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");
    private final UUID departmentId = UUID.randomUUID();
    private final UUID departmentHeadId = UUID.randomUUID();
    private final NotificationType notificationType = NotificationType.GROUP_TRANSFER_DRIVER_FOUND;
    private final UUID passenger = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Водитель назначен",
                "notice_4007",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_DRIVER_FOUND_MZK",
                null,
                "ORGANIZATION",
                "Уважаемый гость, ваш трансфер подтвержден\n" +
                        "        {localDesiredDate}, {localDesiredTime}\n" +
                        "        {carInfo}\n" +
                        "        {departureAddress} - {destinationAddress}\n" +
                        "        С вами свяжется водитель {driverFio} ({driverPhone})\n" +
                        "        Мы позаботимся о вашем комфорте!",
                false,
                true,
                false,
                3600000000000L,
                "BEFORE_DEADLINE",
                null,
                "desiredDate"
        );
        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Водитель назначен",
                "notice_4006",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_DRIVER_FOUND",
                null,
                "ORGANIZATION",

                "На заявку {ID} назначен автомобиль: {carInfo}. Водитель: {driverFio}, {driverPhone}.",
                true,
                true,
                true,

                0,
                "AT_EVENT",
                null,
                null
        );
        transactionTemplate.executeWithoutResult(status -> {
            var department = Department.builder().id(departmentId)
                    .departmentHeadId(departmentHeadId)
                    .organizationId(organizationId).build();

            var department2 = Department.builder().id(UUID.randomUUID())
                    .departmentHeadId(departmentHeadId)
                    .organizationId(UUID.randomUUID()).build();

            departmentRepository.save(department);
            departmentRepository.save(department2);

            Employee employee = new Employee();
            employee.setId(passenger);
            employee.setPhoneConfirmed(true);
            employee.setPhone("+79111111111");
            employee.setOrganizationId(UUID.randomUUID());
            employee.setDepartmentId(department.getId());
            employeeRepository.save(employee);

        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            settingsRepository.deleteAll();
            countingRepository.deleteAll();
            employeeRepository.deleteAll();
            departmentRepository.deleteAll();
            notificationRepository.deleteAll();
            timingRepository.deleteAll();
            channelSettingsRepository.deleteAll();
        });
    }

    @DisplayName("Проверка отправки уведомления после наступления времени")
    @Test
    void testAfterTriggerTime() throws JsonProcessingException {

        NotificationSettings settings = notificationSettingsService.get(organizationId,
                NotificationClass.REQUEST_GROUP_TRANSFER,
                notificationType);

        var actualIimingList = timingService.getOfSettings(settings);
        assertThat(actualIimingList).hasSize(1);

        TripRequest current = new TripRequest();
        current.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(50));
        current.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        current.setApprovalState("APPROVED");
        current.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                Address.builder().region("PetersBurg").build())
                                .build()));
        current.setDriver(DriverDTO.builder()
                        .driverFio("Иванов Иван Иванович")
                .build());
        current.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        current.setAuthorId(passenger);
        current.setPassengerId(passenger);
        current.getInformation().put(CUSTOMER_PHONE_FIELD, "+789998998989");

        command.sendNotification(current);

        verify(smsSender, times(2)).send(any(), any(), any());

        var notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
    }

    @DisplayName("Проверка НЕ отправки уведомления до времени срабатывания триггера")
    @Test
    void testBeforeTriggerTime() throws JsonProcessingException {

        NotificationSettings settings = notificationSettingsService.get(organizationId,
                NotificationClass.REQUEST_GROUP_TRANSFER,
                notificationType);

        var actualIimingList = timingService.getOfSettings(settings);
        assertThat(actualIimingList).hasSize(1);

        TripRequest current = new TripRequest();
        current.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(1).plusMinutes(5));
        current.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        current.setApprovalState("APPROVED");
        current.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                        Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                        Address.builder().region("PetersBurg").build())
                                .build()));
        current.setDriver(DriverDTO.builder()
                .driverFio("Иванов Иван Иванович")
                .build());
        current.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        current.setAuthorId(passenger);
        current.setPassengerId(passenger);
        current.getInformation().put(CUSTOMER_PHONE_FIELD, "+789998998989");

        command.sendNotification(current);

        verify(smsSender, times(1)).send(any(), any(), any());

        var notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).hasSize(1);

    }

    @DisplayName("Проверка НЕ реакции на отсутствие настроки")
    @Test
    void testEntityNotFoundException() throws JsonProcessingException {

        TripRequest current = new TripRequest();
        current.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(50));
        current.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        current.setApprovalState("APPROVED");
        current.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                        Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                        Address.builder().region("PetersBurg").build())
                                .build()));
        current.setDriver(DriverDTO.builder()
                .driverFio("Иванов Иван Иванович")
                .build());
        current.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        current.setAuthorId(passenger);
        current.setPassengerId(passenger);
        current.getInformation().put(CUSTOMER_PHONE_FIELD, "+789998998989");

        transactionTemplate.executeWithoutResult(status -> {
            settingsRepository.deleteAll();
            countingRepository.deleteAll();
            timingRepository.deleteAll();
            channelSettingsRepository.deleteAll();
        });
        command.sendNotification(current);

        verify(smsSender, times(0)).send(any(), any(), any());

        var notifications = notificationRepository.findAll();
        assertThat(notifications).isEmpty();

    }


    @DisplayName("Проверка отправки только одного уведомления если автор и пассажир одинаковы")
    @Test
    void testOnlyOneMessage() throws JsonProcessingException {

        NotificationSettings settings = notificationSettingsService.get(organizationId,
                NotificationClass.REQUEST_GROUP_TRANSFER,
                notificationType);

        var actualIimingList = timingService.getOfSettings(settings);
        assertThat(actualIimingList).hasSize(1);

        TripRequest current = new TripRequest();
        current.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(50));
        current.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        current.setApprovalState("APPROVED");
        current.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                        Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                        Address.builder().region("PetersBurg").build())
                                .build()));
        current.setDriver(DriverDTO.builder()
                .driverFio("Иванов Иван Иванович")
                .build());
        current.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        current.setAuthorId(UUID.fromString(passenger.toString()));
        current.setPassengerId(UUID.fromString(passenger.toString()));

        command.sendNotification(current);

        verify(smsSender, times(1)).send(any(), any(), any());

        var notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
    }
}