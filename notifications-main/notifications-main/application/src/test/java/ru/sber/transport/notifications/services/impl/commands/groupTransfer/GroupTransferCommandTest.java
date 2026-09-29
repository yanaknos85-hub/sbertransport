package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.Address;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.request.Waypoint;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.messaging.listeners.TripRequestHandler;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.services.TripRequestService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications.services.impl.commands=DEBUG",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false"} )
@DisplayName("Проверка сервиса всех настроек уведомлений GroupTransfer")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
class GroupTransferCommandTest extends GroupTransferCommon {
    private static final Logger log = LoggerFactory.getLogger(GroupTransferCommandTest.class);
    public static final String CUSTOMER_PHONE = "+789998998989";
    public static final String DRIVER_ARRIVED_MESSAGE = "Водитель ожидает Вас в точке отправления по адресу {departureAddress}, автомобиль {carInfo}, тел {driver.phoneNumber}";
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


    @MockitoBean
    private SmsSender smsSender;
    @MockitoBean
    private EmailSender emailSender;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TripRequestHandler tripRequestHandler;
    @Autowired
    private GroupTransferAwaitingApprovalCommand awaitingApprovalCommand;
    @Autowired
    private GroupTransferDriverArrivedCommand driverArrivedCommand;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private TripRequestService tripRequestService;

    private final UUID organizationId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");
    private final UUID departmentId = UUID.randomUUID();
    private final UUID departmentHeadId = UUID.randomUUID();
    private final UUID passengerId = UUID.randomUUID();
    private final UUID authorId = UUID.randomUUID();
    private Employee author;
    private Employee passenger;




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
                "На согласовании",
                "notice_4002",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_AWAITING_APPROVAL",
                null,
                "ORGANIZATION",
                
        "Вам поступила заявка {id} на согласование.",
                true,
                true,
                true,
                
        0,
                "AT_EVENT",
                null,
                null
        );

        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Согласовано",
                "notice_4003",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_APPROVED",
                null,
                "ORGANIZATION",
                
        "Ваша заявка {ID} согласована.",
                true,
                true,
                true,
                
        0,
                "AT_EVENT",
                null,
                null
        );
                create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "жидайте назначение водителя",
                "notice_4004",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_AWAITING_SEARCH",
                null,
                "ORGANIZATION",
                
        "Ваша заявка {ID} получена для исполнения. В ближайшее время будет назначен автомобиль и водитель.",
                true,
                true,
                true,
                
        0,
                "AT_EVENT",
                null,
                null
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

        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Водитель ожидает",
                "notice_4009",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_DRIVER_ARRIVED",
                null,
                "ORGANIZATION",
                DRIVER_ARRIVED_MESSAGE,
                true,
                true,
                true,
                0,
                "AT_EVENT",
                null,
                null
        );

        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Поездка завершена",
                "notice_4011",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_TRIP_FINISHED",
                null,
                "ORGANIZATION",                
        "Ваша поездка завершена. Просим оценить сервис.",
                true,
                false,
                true,                
        0,
                "AT_EVENT",
                null,
                null
        );
        create_or_update_notification_with_send_time (
                "REQUEST_GROUP_TRANSFER",
                "Поездка отменена",
                "notice_4012",
                "ac1d7c0e-6ebd-40b4-9595-086dbed05c22",
                null,
                "GROUP_TRANSFER_CANCELLED",
                null,
                "ORGANIZATION",
                "Ваша поездка отменена.",
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
            employee.setId(passengerId);
            employee.setUserId(passengerId);
            employee.setEmail("123@mail.ru");
            employee.setPhoneConfirmed(true);
            employee.setPhone("+79111111111");
            employee.setOrganizationId(UUID.randomUUID());
            employee.setDepartmentId(department.getId());
            passenger = employeeRepository.save(employee);

            Employee employee1 = new Employee();
            employee1.setId(authorId);
            employee1.setUserId(authorId);
            employee1.setEmail("author@mail.ru");
            employee1.setPhoneConfirmed(true);
            employee1.setPhone("+79888888888");
            employee1.setOrganizationId(UUID.randomUUID());
            employee1.setDepartmentId(department.getId());
            author = employeeRepository.save(employee1);
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


    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_AWAITING_APPROVAL")
    @Test
    void testGROUP_TRANSFER_AWAITING_APPROVAL() {

        String currentStatus = TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name();

        TripApprove current = new TripApprove();
        TripRequest request = createTripRequest(currentStatus);
        current.setRequest(request);

        current.setApproverIds(List.of(authorId));
        current.setPassengerId(passengerId);

        assertThat(awaitingApprovalCommand.validate(null, current)).isTrue();
        awaitingApprovalCommand.sendNotification(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(1);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        verify(smsSender, times(1)).send(any(), any(), any());
    }

    @DisplayName("Проверка Согласована	GROUP_TRANSFER_APPROVED	Инициатор	PUSH/SMS/EMAIL")
    @Test
    void testGROUP_TRANSFER_APPROVED() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_APPROVED.name();;

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}", notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(1);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });
        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(1)).send(receiverCaptor.capture(), any(), any());

        var actualReceiver = receiverCaptor.getValue();
        assertThat(actualReceiver).hasSize(1);
        String actualPhone = (String) actualReceiver.get(0);
        assertThat(actualPhone).isEqualTo(author.getPhone());

    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_FOUND")
    @Test
    void testGROUP_TRANSFER_DRIVER_FOUND() {

        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_SEARCH.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();;

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(3);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), any(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(author.getPhone());

        actualReceiver = allValues.get(1);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(passenger.getPhone());


        actualReceiver = allValues.get(2);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(CUSTOMER_PHONE);
    }

    @DisplayName("Проверка отправки уведомления testGROUP_TRANSFER_TRIP_FINISHED")
    @Test
    void testGROUP_TRANSFER_TRIP_FINISHED() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED.name();;

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(1);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(0)).send(any(), any(), any());
        verify(emailSender, times(1)).send(receiverCaptor.capture(), any(), any(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(passenger.getEmail());
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(3);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), messageCaptor.capture(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(author.getPhone());

        actualReceiver = allValues.get(1);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(passenger.getPhone());

        actualReceiver = allValues.get(2);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(CUSTOMER_PHONE);

        // Проверка текста SMS для нового уведомления
        var allMessages = messageCaptor.getAllValues();
        assertThat(allMessages).hasSize(3);
        assertThat(allMessages.get(0)).isEqualTo(DRIVER_ARRIVED_MESSAGE);
        assertThat(allMessages.get(1)).isEqualTo(DRIVER_ARRIVED_MESSAGE);
        assertThat(allMessages.get(2)).isEqualTo(DRIVER_ARRIVED_MESSAGE);

        transactionTemplate.executeWithoutResult(status -> {
            var notification = notificationRepository.findAll().get(0);
            try {
                var request = notification.getEntityFromJson(TripRequest.class);
                assertThat(request.getDepartureAddress()).isEqualTo("Lenina, 2");
                assertThat(request.getCarInfo()).isEqualTo("yellow Brand model номер: E123EW199");
                assertThat(request.getDriver().getPhoneNumber()).isEqualTo("+79111111345");
                assertThat(request.getDriver().getDriverFio()).isEqualTo("Иванов Иван Иванович");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        });

    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED Passenger And Author Equals")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_PassengerAndAuthorEquals() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus, authorId);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(2);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(smsSender, times(2)).send(receiverCaptor.capture(), messageCaptor.capture(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(author.getPhone());

        actualReceiver = allValues.get(1);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(CUSTOMER_PHONE);

        // Проверка текста SMS для нового уведомления
        var allMessages = messageCaptor.getAllValues();
        assertThat(allMessages).hasSize(2);
        assertThat(allMessages.get(0)).isEqualTo(DRIVER_ARRIVED_MESSAGE);
        assertThat(allMessages.get(1)).isEqualTo(DRIVER_ARRIVED_MESSAGE);

        transactionTemplate.executeWithoutResult(status -> {
            var notification = notificationRepository.findAll().get(0);
            try {
                var request = notification.getEntityFromJson(TripRequest.class);
                assertThat(request.getDepartureAddress()).isEqualTo("Lenina, 2");
                assertThat(request.getCarInfo()).isEqualTo("yellow Brand model номер: E123EW199");
                assertThat(request.getDriver().getPhoneNumber()).isEqualTo("+79111111345");
                assertThat(request.getDriver().getDriverFio()).isEqualTo("Иванов Иван Иванович");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        });

    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED с null vehicle")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_nullVehicle() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        previous.setVehicle(null);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), messageCaptor.capture(), any());

        assertThat(messageCaptor.getValue()).isEqualTo(DRIVER_ARRIVED_MESSAGE);
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED с пустыми waypoints")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_emptyWaypoints() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        previous.setWaypoints(List.of());
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), any(), any());
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED с null address в waypoints")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_nullAddressInWaypoint() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        previous.setWaypoints(List.of(Waypoint.builder().address(null).build()));
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), messageCaptor.capture(), any());

        // departureAddress не должен быть установлен, так как address = null
        var allMessages = messageCaptor.getAllValues();
        assertThat(allMessages).hasSize(3);
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED с null waypoints")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_nullWaypoints() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest previous = createTripRequest(previousStatus);
        previous.setWaypoints(null);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), any(), any());
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED")
    @Test
    void testGROUP_TRANSFER_CANCELLED() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_CANCELLED.name();

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(1);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(1)).send(receiverCaptor.capture(), any(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(author.getPhone());

    }


    @DisplayName("Проверка Ожидайте назначение водителя GROUP_TRANSFER_AWAITING_SEARCH Пассажир/инициатор PUSH/SMS/EMAIL")
    @Test
    void testGROUP_TRANSFER_AWAITING_SEARCH() {
        String previousStatus = TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS.name();
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH.name();;

        TripRequest previous = createTripRequest(previousStatus);
        var current = createRequestMessage(previous, currentStatus);

        tripRequestHandler.handle(current);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            notifications.forEach(notification -> {log.info("Customer {},  {}",notification.getCustomer(),notification.getSettings().getDescription());});
            assertThat(notifications).hasSize(2);
            assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).isEmpty();
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(2)).send(receiverCaptor.capture(), any(), any());

        var allValues = receiverCaptor.getAllValues();
        var actualReceiver = allValues.get(0);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(author.getPhone());

        actualReceiver = allValues.get(1);
        assertThat(actualReceiver).hasSize(1);
        assertThat((String) actualReceiver.get(0)).isEqualTo(passenger.getPhone());
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED напрямую командой")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_directCommand_nullVehicle() throws JsonProcessingException {
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest request = createTripRequestForCommandTest(currentStatus);
        request.setVehicle(null);
        request.setWaypoints(List.of(Waypoint.builder()
                .address(Address.builder().region("Moscow").street("Lenina").house("2").build())
                .build()));

        driverArrivedCommand.sendNotification(request);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), messageCaptor.capture(), any());

        var allMessages = messageCaptor.getAllValues();
        assertThat(allMessages).hasSize(3);
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED напрямую командой с null address")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_directCommand_nullAddress() throws JsonProcessingException {
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest request = createTripRequestForCommandTest(currentStatus);
        request.setVehicle(VehicleDTO.builder().carInfo("Test Car").build());
        request.setWaypoints(List.of(Waypoint.builder().address(null).build()));

        driverArrivedCommand.sendNotification(request);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), any(), any());
    }

    @DisplayName("Проверка отправки уведомления GROUP_TRANSFER_DRIVER_ARRIVED напрямую командой с null waypoints")
    @Test
    void testGROUP_TRANSFER_DRIVER_ARRIVED_directCommand_nullWaypoints() throws JsonProcessingException {
        String currentStatus = TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED.name();

        TripRequest request = createTripRequestForCommandTest(currentStatus);
        request.setVehicle(VehicleDTO.builder().carInfo("Test Car").build());
        request.setWaypoints(null);

        driverArrivedCommand.sendNotification(request);

        transactionTemplate.executeWithoutResult(status -> {
            var notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(3);
        });

        var receiverCaptor = ArgumentCaptor.forClass(List.class);

        verify(smsSender, times(3)).send(receiverCaptor.capture(), any(), any());
    }
    private RequestMessage createRequestMessage(TripRequest previous, String currentStatus) {
        return createRequestMessage(previous, currentStatus, passengerId);
    }
    private RequestMessage createRequestMessage(TripRequest previous, String currentStatus, UUID passengerId) {
        return RequestMessage.builder()
                .id(previous.getId())
                .desiredDate(previous.getDesiredDate())
                .timeZone(TimeZone.getDefault().toZoneId().toString())
                .approvalState("APPROVED")
                .transportType("GROUP_TRANSFER")
                .waypoints(List.of(
                        new RequestMessage.Waypoint(UUID.randomUUID(),
                                AddressMessage.builder().region("Moscow").street("Lenina").house("2").build(),
                                Duration.ofMillis(10000000),
                                false,
                                false,
                                "absenceReason",
                                0),
                        new RequestMessage.Waypoint(UUID.randomUUID(),
                                AddressMessage.builder().region("SPB").build(),
                                Duration.ofMillis(10000000),
                                false,
                                false,
                                "absenceReason",
                                1)))
                .driverData(RequestMessage.DriverData.builder()
                        .lastName("Иванов")
                        .firstName("Иван")
                        .patronymic("Иванович")
                        .phoneNumber("+79111111345")
                        .build())
                .vehicleData(RequestMessage.VehicleData.builder()
                        .brand("Brand")
                        .color("yellow")
                        .model("model")
                        .stateNumber("E123EW199")
                        .build())
                .authorId(authorId)
                .author(RequestMessage.Employee.builder()
                        .userId(authorId)
                        .build())
                .passengerId(passengerId)
                .passenger(RequestMessage.Employee.builder()
                        .userId(passengerId)
                        .build())
                .information(Map.of("addContactPhone", CUSTOMER_PHONE))
                .status(currentStatus)
                .approvalId(UUID.randomUUID())
                .build();
    }

    @NotNull
    private TripRequest createTripRequest(String previousStatus) {
        TripRequest previous = new TripRequest();
        previous.setId(UUID.randomUUID());
        previous.setTransportType(TransportTypeEnum.GROUP_TRANSFER);
        previous.setStatus(previousStatus);
        previous.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(30));
        previous.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        previous.setApprovalState("APPROVED");
        previous.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                        Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                        Address.builder().region("PetersBurg").build())
                                .build()));
        previous.setDriver(DriverDTO.builder()
                .driverFio("Иванов Иван Иванович")
                .build());
        previous.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        previous.setAuthorId(authorId);
        previous.setPassengerId(passengerId);
        previous.setPassenger(passenger);
        previous.setAuthor(author);
        previous.getInformation().put("addContactPhone", CUSTOMER_PHONE);
        tripRequestService.save(previous);
        return previous;
    }

    @NotNull
    private TripRequest createTripRequestForCommandTest(String previousStatus) {
        TripRequest previous = new TripRequest();
        previous.setId(UUID.randomUUID());
        previous.setTransportType(TransportTypeEnum.GROUP_TRANSFER);
        previous.setStatus(previousStatus);
        previous.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(30));
        previous.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        previous.setApprovalState("APPROVED");
        previous.setAuthorId(authorId);
        previous.setPassengerId(passengerId);
        previous.setPassenger(passenger);
        previous.setAuthor(author);
        previous.getInformation().put("addContactPhone", CUSTOMER_PHONE);
        tripRequestService.save(previous);
        return previous;
    }

}