package ru.sber.transport.notifications.commands;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.ExpectedData;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.mapper.trip_request.WaypointMapper;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request.messaging.TaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@AutoConfigureMockMvc
@DisplayName("Проверка отправки уведомлений заявок на такси")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class TaxiCommandsTest extends SharedCommands {

    @Autowired
    private TripRequestRepository requestRepository;

    @Autowired
    private TripApproveRepository approveRepository;

    @Autowired
    private WaypointMapper waypointMapper;

    @Test
    @DisplayName("Проверка отправки уведомления 'Согласование заявки' (notice_101)")
    void test_requestApprovingCommand() {

        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());
        deadlineSettingsMessageInput.accept(MessageBuilder.withPayload(deadlineMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());
        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var tokens = requestMessage.getApprovalId();

        var approveTripRequestMessage = new ApproveTripRequestMessage(
                requestMessage.getId(),
                null,
                requestMessage.getApprovalId(),
                null,
                List.of(approverEmployeeMessage.getId())
        );

        approveTripRequestMessageInput.accept(MessageBuilder.withPayload(approveTripRequestMessage).build());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Статус согласования заявки' (notice_102)")
    void test_requestApproveStatusCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        TripApprove tripApprove = new TripApprove();
        tripApprove.setApproverId(requestMessage.getApprovalId());
        tripApprove.setRequestId(requestMessage.getId());
        tripApprove.setPassengerId(requestMessage.getPassengerId());
        tripApprove.setApproveStatus(ApproveStatus.NEW);
        tripApprove.setStatus(null);
        tripApprove.setDesiredDate(requestMessage.getDesiredDate());
        approveRepository.saveAndFlush(tripApprove);

        var approveTripRequestMessage = new ApproveTripRequestMessage(
                requestMessage.getId(),
                true,
                requestMessage.getApprovalId(),
                null,
                List.of(approverEmployeeMessage.getId())
        );
        var tokens = requestMessage.getPassengerId();

        approveTripRequestMessageInput.accept(MessageBuilder.withPayload(approveTripRequestMessage).build());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Ожидайте назначения водителя' (notice_103)")
    void test_WaitingForDriverCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Ожидайте назначения водителя. Мы ищем для Вас лучшее решение.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_SEARCH.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("APPROVED")
                .build();
        assertThat(requestRepository.count()).isEqualTo(1);

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Назначение водителя' (notice_104)")
    void test_DriverFoundCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        var taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                null,
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                null,
                requestMessage.getRideId(),
                null,
                false,
                new TaxiTripMessage.Driver("Фамилия", "Имя" , "Отчество", "Телефон"),
                new TaxiTripMessage.Vehicle("Запорожец", "Горбатый", "Красный", "О111ОО")
        );

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_FOUND.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(requestMessage.getRideId())
                .approvalState("APPROVED")
                .build();
        assertThat(requestRepository.count()).isEqualTo(1);

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_DRIVER_FOUND.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Назначение водителя' (notice_104). XML")
    @Disabled("Требуется переработка")
    void test_DriverFoundCommand_resolution() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Заказ будет выполнен. {resolution}";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        var taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                "Решение по заявке",
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                null,
                requestMessage.getRideId(),
                null,
                false,
                null,
                null
        );

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_FOUND.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(requestMessage.getRideId())
                .approvalState("APPROVED")
                .build();
        assertThat(requestRepository.count()).isEqualTo(1);

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_DRIVER_FOUND.name());

        notificationMessage = notificationMessage
                .replace("{resolution}", taxiTripMessage.resolution())
        ;
        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Водитель ожидает в пункте отправления' (notice_105)")
    void test_DriverArrivedCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Водитель {driver.lastName} {driver.firstName} {driver.patronymic} ожидает в пункте" +
                " назначения, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        assertThat(requestRepository.count()).isZero();

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        assertThat(requestRepository.count()).isEqualTo(1);
        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("APPROVED")
                .build();

        assertThat(requestRepository.count()).isEqualTo(1);

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_DRIVER_ARRIVED.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Поездка началась' (notice_107)")
    void test_tripInProgressCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        driverMessageInput.accept(MessageBuilder.withPayload(driverMessage).build());

        var notificationMessage = "Поездка началась, время в пути составит {taxiTrip.tripFactDuration}";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        assertThat(requestRepository.count()).isZero();
        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        var taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                "Красный Запорожец О111ОО Рандомов Рандом Рандомович",
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                requestMessage.getId(),
                requestMessage.getRideId(),
                null,
                false,
                new TaxiTripMessage.Driver("Фамилия", "Имя" , "Отчество", "Телефон"),
                null
        );

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .driverId(driverMessage.getId())
                .approvalState("APPROVED")
                .build();

        assertThat(requestRepository.count()).isEqualTo(1);

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name());

        notificationMessage =
                notificationMessage.replace("{tripFactDuration}",
                        "%02d:%02d".formatted(taxiTripMessage.tripFactDuration().toMinutes(), taxiTripMessage.tripFactDuration().toSecondsPart()));
        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Прибытие в промежуточный пункт' (notice_108)")
    void test_waypointArrivedCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        driverMessageInput.accept(MessageBuilder.withPayload(driverMessage).build());

        var notificationMessage = "Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания " +
                "составляет {tariff.waitCostPerMinIntermediate} руб./мин.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        taxiTariffMessageInput.accept(MessageBuilder.createMessage(tariffMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, tariffMessage.getId()))));

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(List.of(
                        createWaypoint(waypoints.getFirst().id(), true, 0),
                        createWaypoint(waypoints.get(1).id(), true, 1),
                        waypoints.get(2),
                        waypoints.get(3)
                ))
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("APPROVED")
                .build();

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Order(10)
    @Test
    @DisplayName("Проверка отправки уведомления 'Поездка завершена' (notice_110)")
    void test_tripFinishedCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        driverMessageInput.accept(MessageBuilder.withPayload(driverMessage).build());

        var notificationMessage = "Поездка завершена.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        taxiTariffMessageInput.accept(MessageBuilder.createMessage(tariffMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, tariffMessage.getId()))));

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        requestMessage = RequestMessage.builder()
                .id(requestMessage.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("APPROVED")
                .build();

        var tokens = requestMessage.getPassengerId();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_FINISHED.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Order(11)
    @Test
    @DisplayName("Проверка отправки уведомления 'Присоединение к совместной поездке' (notice_111)")
    void test_coopTripAttachmentCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        driverMessageInput.accept(MessageBuilder.withPayload(driverMessage).build());

        var notificationMessage = "К вашей поездке присоединился {passenger.lastName} {passenger.firstName} " +
                "{passenger.patronymic}";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage2).build());

        taxiTariffMessageInput.accept(MessageBuilder.createMessage(tariffMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, tariffMessage.getId()))));

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        var tokens = requestMessage.getPassengerId();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        var taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                "Красный Запорожец О111ОО Рандомов Рандом Рандомович",
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                null,
                requestMessage.getRideId(),
                null,
                false,
                new TaxiTripMessage.Driver("Фамилия", "Имя" , "Отчество", "Телефон"),
                null
        );

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .driverId(driverMessage.getId())
                .passengerId(passengerMessage2.getId())
                .approvalState("APPROVED")
                .rideId(requestMessage.getRideId())
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(2);

        verifyTokensAndMessages(notificationMessage, tokens);
    }

    @Order(12)
    @Test
    @DisplayName("Проверка отправки уведомления 'Изменение в совместной поездке' (notice_112)")
    void test_coopTripChangesCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        driverMessageInput.accept(MessageBuilder.withPayload(driverMessage).build());

        var notificationMessage = "Пассажир {passenger.lastName} {passenger.firstName} {passenger.patronymic} " +
                "отказался от совместной поездки с вами № {magentaSharedRequest.magentaId}.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage2).build());

        var tokens = requestMessage.getPassengerId();

        taxiTariffMessageInput.accept(MessageBuilder.createMessage(tariffMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, tariffMessage.getId()))));

        var taxiTripMessage = new TaxiTripMessage(
                TripType.COOP.name(),
                UUID.randomUUID(),
                500000 + "",
                organizationMessage.getId(),
                "Красный Запорожец О111ОО Рандомов Рандом Рандомович",
                UUID.randomUUID(),
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(5),
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusSeconds(10),
                null,
                InboundTaxiTripStatus.TRIP_IN_PROGRESS.name(),
                4.70,
                Duration.ofMinutes(15).plusHours(5),
                15600,
                Duration.ofMinutes(5),
                null,
                null,
                requestMessage.getRideId(),
                null,
                false,
                new TaxiTripMessage.Driver("Фамилия", "Имя" , "Отчество", "Телефон"),
                null
        );

        var requestMessage = RequestMessage.builder()
                .id(UUID.randomUUID())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage1.getId())
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .driverId(driverMessage.getId())
                .approvalState("NEW")
                .build();

        taxiTripMessageInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        TripRequest tripRequest = TripRequest.builder()
                .id(UUID.randomUUID())
                .passengerId(passengerMessage2.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(ExpectedData.builder().cost(100D).time(Duration.ZERO).distance(200D).build())
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI)
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypointMapper.toEntity(waypoints))
                .build();
        requestRepository.save(tripRequest);
        assertThat(requestRepository.count()).isEqualTo(2);

        requestMessage = RequestMessage.builder()
                .id(tripRequest.getId())
                .purposeId(UUID.randomUUID())
                .approvalId(approverEmployeeMessage.getId())
                .deleted(false)
                .authorId(UUID.randomUUID())
                .coopTrip(true)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .desiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                .expected(new RequestMessage.ExpectedData(
                        100D,
                        200D,
                        Duration.ZERO
                ))
                .finishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(passengerMessage2.getId())
                .status(TripRequestStatus.TAXI_CANCELLED.name())
                .tariffId(tariffMessage.getId())
                .transportType(TransportTypeEnum.TAXI.name())
                .tripClass(tariffMessage.taxiClass())
                .waypoints(waypoints)
                .rideId(requestMessage.getRideId())
                .driverId(driverMessage.getId())
                .passengerId(tripRequest.getPassengerId())
                .approvalState("APPROVED")
                .rideId(requestMessage.getRideId())
                .build();
        
        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        verifyTokenAndMessage(notificationMessage, tokens);
    }
}
