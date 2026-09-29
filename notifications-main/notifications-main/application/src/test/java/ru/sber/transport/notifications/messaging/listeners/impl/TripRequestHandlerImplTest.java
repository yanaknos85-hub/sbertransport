package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@EmbeddedPostgres
@DisplayName("Проверка слушателя заявок")
@Feature("app_platform_notifications")
@SpringBootTest(properties = "NOTIFICATION_SETTINGS_CRON_VALUE=12 * * * * *")
class TripRequestHandlerImplTest {

    @Autowired
    private TripRequestRepository repository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private Processor<TripRequest> processor;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private PushSender pushSender;

    @MockitoBean
    private SmsSender smsSender;

    @Autowired
    @Qualifier("tripRequestInput")
    private Consumer<Message<RequestMessage>> requestMessageInput;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Получение нового")
    void test_new() {
        var message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .purposeId(UUID.randomUUID())
            .approvalId(UUID.randomUUID())
            .deleted(false)
            .authorId(UUID.randomUUID())
            .coopTrip(true)
            .commentForDriver("Comment")
            .creationTime(LocalDateTime.now())
            .desiredDate(LocalDateTime.now().plusDays(1))
            .expected(new RequestMessage.ExpectedData(100D, 200D, Duration.ZERO))
            .finishedTime(LocalDateTime.now().plusDays(2))
            .humanReadableId("HRI")
            .passengerCount(3)
            .passengerId(UUID.randomUUID())
            .status("TAXI_APPROVED")
            .tariffId(UUID.randomUUID())
            .transportType("TAXI")
            .tripClass("ECONOMY")
            .approvalState("NEW")
            .build();

        assertThat(repository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        checkTripRequest(message, actual);
    }

    @Test
    @DisplayName("Удаление")
    void test_delete() throws JsonProcessingException {
        var tripPurpose = new TripRequest();
        tripPurpose.setId(UUID.randomUUID());

        transactionTemplate.execute(s->
            repository.save(tripPurpose)
        );

        var message = RequestMessage.builder()
            .id(tripPurpose.getId())
            .deleted(true)
            .build();

        assertThat(repository.count()).isEqualTo(1);

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isZero();
        verify(processor, never()).process(any(TripRequest.class));
    }

    @Test
    @DisplayName("Получение заявки с совместной поездкой")
    void test_sharedRequest() {
        var message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .purposeId(UUID.randomUUID())
            .approvalId(UUID.randomUUID())
            .deleted(false)
            .authorId(UUID.randomUUID())
            .coopTrip(true)
            .commentForDriver("Comment")
            .creationTime(LocalDateTime.now())
            .desiredDate(LocalDateTime.now().plusDays(1))
            .expected(new RequestMessage.ExpectedData(100D, 200D, Duration.ZERO))
            .finishedTime(LocalDateTime.now().plusDays(2))
            .humanReadableId("HRI")
            .passengerCount(3)
            .passengerId(UUID.randomUUID())
            .status("TAXI_APPROVED")
            .tariffId(UUID.randomUUID())
            .transportType("TAXI")
            .tripClass("ECONOMY")
            .rideId(UUID.randomUUID())
            .approvalState("APPROVED")
            .build();
        assertThat(repository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .purposeId(UUID.randomUUID())
            .approvalId(UUID.randomUUID())
            .deleted(false)
            .authorId(UUID.randomUUID())
            .coopTrip(true)
            .commentForDriver("Comment")
            .creationTime(LocalDateTime.now())
            .desiredDate(LocalDateTime.now().plusDays(1))
            .expected(new RequestMessage.ExpectedData(100D, 200D, Duration.ZERO))
            .finishedTime(LocalDateTime.now().plusDays(2))
            .humanReadableId("HRI")
            .passengerCount(3)
            .passengerId(UUID.randomUUID())
            .status("TAXI_APPROVED")
            .tariffId(UUID.randomUUID())
            .transportType("TAXI")
            .tripClass("ECONOMY")
            .rideId(message.getRideId())
            .approvalState("NEW")
            .build();

        assertThat(repository.count()).isEqualTo(1);

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .purposeId(UUID.randomUUID())
            .approvalId(UUID.randomUUID())
            .deleted(false)
            .authorId(UUID.randomUUID())
            .coopTrip(true)
            .commentForDriver("Comment")
            .creationTime(LocalDateTime.now())
            .desiredDate(LocalDateTime.now().plusDays(1))
            .expected(new RequestMessage.ExpectedData(100D, 200D, Duration.ZERO))
            .finishedTime(LocalDateTime.now().plusDays(2))
            .humanReadableId("HRI")
            .passengerCount(3)
            .passengerId(UUID.randomUUID())
            .status("TAXI_APPROVED")
            .tariffId(UUID.randomUUID())
            .transportType("TAXI")
            .tripClass("ECONOMY")
            .rideId(message.getRideId())
            .approvalState("APPROVED")
            .build();

        assertThat(repository.count()).isEqualTo(2);

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.findAllBySharedRideId(message.getRideId())).hasSize(3);
    }

    @ParameterizedTest
    @MethodSource("testData")
    @DisplayName("Проверка отправки Push для разных cargo-event")
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED) )
    @Sql(scripts = "/sql/user_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED) )
    void test_cargo_new(String prevStatus, String currentStatus, String text, int statusCode, String type) {

        var authorId = UUID.fromString("91211794-216d-4b92-9ae9-2aeea94e0d2d");
        var orgAndDepId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");

        var message = buildRequest(orgAndDepId, authorId, orgAndDepId, prevStatus, 0, type, LocalDateTime.now().plusDays(2));

        assertThat(repository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        checkTripRequest(message, actual);

        message = buildRequest(orgAndDepId, authorId, orgAndDepId, currentStatus, statusCode, type, LocalDateTime.now().plusDays(4));

        assertThat(repository.count()).isEqualTo(1);

        when(employeeService.get(any())).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));
        when(departmentService.get(any())).thenReturn(Optional.of(buildDepartment(orgAndDepId, authorId)));
        when(employeeRepository.findById(authorId)).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsSender, never()).send(any(), any(), any());
        verify(emailSender, never()).send(any(), any(), any(), any());
        verify(pushSender).send(any(UUID.class), any(), any(), templateCaptor.capture(), any(), any());
        var actualProc = templateCaptor.getValue();
        assertThat(actualProc).isEqualTo(text);

        assertTrue(actualProc.contains(text));

        verify(smsSender, never()).send(any(), any(), any());
        verify(emailSender, never()).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Проверка отправки SMS на остустствие дубликатов")
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/user_settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_cargo_duplicates() {
        //"CARGO_ACCEPTED", "CARGO_AWAITING_APPROVAL", "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}", 0, "INDIVIDUAL"
        String prevStatus =  "CARGO_ACCEPTED";
        String currentStatus = "CARGO_AWAITING_APPROVAL";
        String text = "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}";
        int statusCode = 0;
        String type = "INDIVIDUAL";

        var authorId = UUID.fromString("91211794-216d-4b92-9ae9-2aeea94e0d2d");
        var orgAndDepId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");

        LocalDateTime desiredDate = LocalDateTime.now().plusDays(2);
        var message = buildRequest(orgAndDepId, authorId, orgAndDepId, prevStatus, 0, type, desiredDate);

        assertThat(repository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        checkTripRequest(message, actual);

        //Arguments.of("CARGO_AWAITING_APPROVAL", "CARGO_APPROVED", "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", 0, "INDIVIDUAL"),
        prevStatus =  currentStatus;
        currentStatus = "CARGO_APPROVED";
        text = "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}";
        statusCode = 0;
        type = "INDIVIDUAL";

        message = buildRequest(orgAndDepId, authorId, orgAndDepId, currentStatus, statusCode, type, desiredDate);

        assertThat(repository.count()).isEqualTo(1);

        when(employeeService.get(any())).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));
        when(departmentService.get(any())).thenReturn(Optional.of(buildDepartment(orgAndDepId, authorId)));
        when(employeeRepository.findById(authorId)).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());


        assertThat(repository.count()).isEqualTo(1);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsSender, never()).send(any(), any(), any());
        verify(emailSender, never()).send(any(), any(), any(), any());
        verify(pushSender, times(1)).send(any(UUID.class), any(), any(), templateCaptor.capture(), any(), any());
        var actualProc = templateCaptor.getValue();
        assertThat(actualProc).isEqualTo(text);

        transactionTemplate.executeWithoutResult(s->{
                notificationRepository.findAll().forEach(notification -> {
                    log.info("Notification: id = {}, name = {}",
                            notification.getId(),
                            notification.getSettings().getName());
                });
        });

        assertThat(notificationRepository.count()).isEqualTo(1);

        assertTrue(actualProc.contains(text));
        verify(smsSender, never()).send(any(), any(), any());
        verify(emailSender, never()).send(any(), any(), any(), any());
    }

    private Employee buildEmployee(UUID authorId, UUID orgAndDepId) {
        return Employee.builder()
            .id(authorId)
            .departmentId(orgAndDepId)
            .firstName("firstName")
            .lastName("lastName")
            .patronymic("patronymic")
            .build();
    }

    @ParameterizedTest
    @MethodSource("groupTransferData")
    @DisplayName("Проверка отправки SMS для group transfer")
    @Sql(scripts = {"/sql/group_transfer_settings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_transfer_group_new(String prevStatus, String currentStatus, String text, int statusCode, String type) {

        var authorId = UUID.fromString("91211794-216d-4b92-9ae9-2aeea94e0d2d");
        var orgAndDepId = UUID.fromString("ac1d7c0e-6ebd-40b4-9595-086dbed05c22");

        var message = buildRequest(orgAndDepId, authorId, orgAndDepId, prevStatus, 0, type);

        assertThat(repository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        checkTripRequest(message, actual);

        message = buildRequest(orgAndDepId, authorId, orgAndDepId, currentStatus, statusCode, type);

        assertThat(repository.count()).isEqualTo(1);

        when(employeeService.get(any())).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));
        when(departmentService.get(any())).thenReturn(Optional.of(buildDepartment(orgAndDepId, authorId)));
        when(employeeRepository.findById(authorId)).thenReturn(Optional.of(buildEmployee(authorId, orgAndDepId)));
        requestMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var tripCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsSender).send(any(), tripCaptor.capture(), any());
        var actualProc = tripCaptor.getValue();
        assertThat(actualProc).isEqualTo(text);

    }

    @AfterEach
    void cleanup() {
        transactionTemplate.executeWithoutResult(s -> {
            entityManager.createQuery("DELETE FROM UserNotificationSettings ns").executeUpdate();
            entityManager.createQuery("DELETE FROM Notification n").executeUpdate();
            entityManager.createQuery("DELETE FROM ChannelSettings c").executeUpdate();
            entityManager.createQuery("DELETE FROM TimingSettings ts").executeUpdate();
            entityManager.createQuery("DELETE FROM CountingSettings ts").executeUpdate();
            entityManager.createQuery("DELETE FROM RestrictionSettings ts").executeUpdate();
            entityManager.createQuery("DELETE FROM NotificationSettings ns").executeUpdate();
            entityManager.createQuery("DELETE FROM Employee e").executeUpdate();
            entityManager.createQuery("DELETE FROM TripRequest t").executeUpdate();
        });
    }

    private RequestMessage.Employee buildMsgEmployee(UUID authorId, UUID orgAndDepId) {
        return RequestMessage.Employee.builder()
            .userId(authorId)
            .departmentId(orgAndDepId)
            .firstName("firstName")
            .lastName("lastName")
            .patronymic("patronymic")
            .organizationId(orgAndDepId)
            .build();
    }

    private RequestMessage buildRequest(UUID id, UUID authorId, UUID orgAndDepId, String status, int statusCode, String type) {
        return buildRequest(id, authorId, orgAndDepId, status, statusCode, type, LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(12).plusMinutes(10).plusDays(1));
    }
    private RequestMessage buildRequest(UUID id, UUID authorId, UUID orgAndDepId, String status, int statusCode, String type, LocalDateTime desiredDate) {

        int count = 3;
        List<AddressMessage> addresss = IntStream.range(0, count - 1)
            .mapToObj(this::getAddress)
            .toList();

        var waypoints = IntStream.range(0, count - 1)
            .mapToObj(i -> this.buildWaypoint(addresss, i))
            .toList();

        var msg = RequestMessage.builder()
            .id(id)
            .transportType(type)
            .author(buildMsgEmployee(authorId, orgAndDepId))
            .purposeId(UUID.randomUUID())
            .approvalId(UUID.randomUUID())
            .approvalState(ApprovalState.AWAITING_APPROVAL.name())
            .deleted(false)
            .authorId(authorId)
            .coopTrip(true)
            .commentForDriver("Comment")
            .creationTime(LocalDateTime.now())
            .desiredDate(desiredDate)
            .expected(new RequestMessage.ExpectedData(100D, 200D, Duration.ZERO))
            .finishedTime(LocalDateTime.now().plusDays(2))
            .humanReadableId("HRI")
            .passengerCount(3)
            .timeZone(ZoneOffset.UTC.toString())
            .status(status)
            .tariffId(UUID.randomUUID())
            .tripClass("ECONOMY")
            .statusCode(statusCode)
            .information(Map.of("addContactPhone", "+7000"))
            .driverData(new RequestMessage.DriverData("DriverLastName", "DriverFirstName", "DriverPatronymic", "DriverPhoneNumber"))
            .vehicleData(new RequestMessage.VehicleData("VehicleBrand", "VehicleModel", "VehicleStateNumber", "VehicleColor"))
            .waypoints(waypoints);

        if (statusCode == 202) {
            msg.approvalState(ApprovalState.DECLINED.name());
            msg.statusCode(202);
        }

        return msg.build();
    }

    private AddressMessage getAddress(int i) {
        return AddressMessage.builder()
            .region("Регион " + i)
            .city("Город" + i)
            .street("улица " + i)
            .house("" + i)
            .build();
    }

    private static Stream<Arguments> testDuplicates() {
        return Stream.of(
                Arguments.of("CARGO_ACCEPTED", "CARGO_AWAITING_APPROVAL", "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}", 0, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_APPROVAL", "CARGO_APPROVED", "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", 0, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_APPROVAL", "CARGO_CANCELED", "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", 202, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_DATA", "CARGO_AWAITING_TRANSFER", "На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}", 0, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_DATA", "CARGO_AWAITING_TRANSFER", "На заявку {humanReadableId} назначен курьер", 0, "COURIER"),
                Arguments.of("CARGO_TRANSFER_FINISHED", "CARGO_SHIPMENT_FINISHED", "Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг", 0, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_TRANSFER", "CARGO_CANCELED", "Заявка {humanReadableId} отменена контрагентом", 803, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_DATA", "CARGO_CANCELED", "Заявка {humanReadableId} отменена контрагентом", 803, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_TRANSFER", "CARGO_CANCELED", "Заявка {humanReadableId} отменена Инженером", 801, "INDIVIDUAL"),
                Arguments.of("CARGO_AWAITING_DATA", "CARGO_CANCELED", "Заявка {humanReadableId} отменена Инженером", 801, "INDIVIDUAL")

        );
    }

    private static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of("CARGO_ACCEPTED", "CARGO_AWAITING_APPROVAL", "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}", 0, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_APPROVAL", "CARGO_APPROVED", "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", 0, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_APPROVAL", "CARGO_CANCELED", "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", 202, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_DATA", "CARGO_AWAITING_TRANSFER", "На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}", 0, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_DATA", "CARGO_AWAITING_TRANSFER", "На заявку {humanReadableId} назначен курьер", 0, "COURIER"),
            Arguments.of("CARGO_TRANSFER_FINISHED", "CARGO_SHIPMENT_FINISHED", "Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг", 0, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_TRANSFER", "CARGO_CANCELED", "Заявка {humanReadableId} отменена контрагентом", 803, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_DATA", "CARGO_CANCELED", "Заявка {humanReadableId} отменена контрагентом", 803, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_TRANSFER", "CARGO_CANCELED", "Заявка {humanReadableId} отменена Инженером", 801, "INDIVIDUAL"),
            Arguments.of("CARGO_AWAITING_DATA", "CARGO_CANCELED", "Заявка {humanReadableId} отменена Инженером", 801, "INDIVIDUAL")
           //, Arguments.of("CARGO_APPROVED", "CARGO_APPROVED", "По заявке {humanReadableId} изменен плановый срок исполнения на {desiredDate}", 818, "INDIVIDUAL")
        );
    }

    private Department buildDepartment(UUID orgAndDepId, UUID authorId) {
        return Department.builder()
            .id(orgAndDepId)
            .departmentHeadId(authorId)
            .organizationId(orgAndDepId)
            .build();
    }

    private void checkTripRequest(RequestMessage message, TripRequest actual) {
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getPurposeId()).isEqualTo(message.getPurposeId());
        assertThat(actual.getApprovalId()).isEqualTo(message.getApprovalId());
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.isCoopTrip()).isEqualTo(message.isCoopTrip());
        assertThat(actual.getCommentForDriver()).isEqualTo(message.getCommentForDriver());
        assertThat(actual.getCreationTime()).isCloseTo(message.getCreationTime(), within(1, ChronoUnit.MILLIS));
        assertThat(actual.getDesiredDate()).isCloseTo(message.getDesiredDate(), within(1, ChronoUnit.MILLIS));
        assertThat(actual.getExpected().getCost()).isEqualTo(message.getExpected().cost());
        assertThat(actual.getExpected().getDistance()).isEqualTo(message.getExpected().distance());
        assertThat(actual.getExpected().getTime()).isEqualTo(message.getExpected().time());
        assertThat(actual.getFinishedTime()).isCloseTo(message.getFinishedTime(), within(1, ChronoUnit.MILLIS));
        assertThat(actual.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
        assertThat(actual.getPassengerCount()).isEqualTo(message.getPassengerCount());
        assertThat(actual.getPassengerId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getStatus()).isEqualTo(message.getStatus());
        assertThat(actual.getTariffId()).isEqualTo(message.getTariffId());
        assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.valueOf(message.getTransportType()));
        assertThat(actual.getTripClass()).isEqualTo(message.getTripClass());
    }

    private RequestMessage.Waypoint buildWaypoint(List<AddressMessage> adresss, int i) {
        return new RequestMessage.Waypoint(UUID.randomUUID(),
            adresss.get(i),
            Duration.ofMillis(10000000),
            false,
            false,
            "absenceReason",
            i);
    }

    private static Stream<Arguments> groupTransferData() {
        return Stream.of(
            Arguments.of("GROUP_TRANSFER_DRIVER_SEARCH", "GROUP_TRANSFER_DRIVER_FOUND", "Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}", 1, "GROUP_TRANSFER"),
            Arguments.of("GROUP_TRANSFER_AWAITING_SEARCH", "GROUP_TRANSFER_DRIVER_FOUND", "Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}", 1, "GROUP_TRANSFER"));
    }
}