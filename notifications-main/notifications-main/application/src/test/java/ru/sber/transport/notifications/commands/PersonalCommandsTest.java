package ru.sber.transport.notifications.commands;

import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.deadline.DeadlineSettingsRepository;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.contractor.DriverRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TaxiTripRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripSharedRideRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.ExpectedData;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.mapper.trip_request.WaypointMapper;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Transactional
@SpringBootTest(properties = "logging.level.ru.sber.transport.notifications.services.impl.commands=DEBUG")
@AutoConfigureMockMvc
@DisplayName("Проверка отправки уведомлений заявок на личный транспорт")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
class PersonalCommandsTest extends SharedCommands {

    @Autowired
    private TripRequestRepository requestRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TripSharedRideRepository tripSharedRideRepository;
    
    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TaxiTripRepository taxiTripRepository;
    
    @Autowired
    private TripApproveRepository approveRepository;
    
    @Autowired
    private DeadlineSettingsRepository deadlineSettingRepository;
    
    @Autowired
    private WaypointMapper waypointMapper;
    
    @Test
    @DisplayName("Проверка отправки уведомления 'Согласование заявки' (notice_301)")
    void test_requestApprovingCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());
        deadlineSettingsMessageInput.accept(MessageBuilder.withPayload(deadlineMessage).build());
        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.";
        
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());
        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

        var requestMessage = defaultRequestBuilder()
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                .build();

        assertThat(requestRepository.count()).isZero();

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
    @DisplayName("Проверка отправки уведомления 'Статус согласования заявки' (notice_302)")
    void test_requestApproveStatusCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

        UUID id = UUID.randomUUID();
        var requestMessage = defaultRequestBuilder(id)
                .approvalState("NEW")
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                .waypoints(waypoints)
                .build();
        
        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        
        TripApprove tripApprove = new TripApprove();
        tripApprove.setApproverId(requestMessage.getApprovalId());
        tripApprove.setRequestId(requestMessage.getId());
        tripApprove.setPassengerId(requestMessage.getPassengerId());
        tripApprove.setApproveStatus(ApproveStatus.NEW);
        tripApprove.setDesiredDate(requestMessage.getDesiredDate());
        tripApprove.setStatus(null);
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
    @DisplayName("Проверка отправки уведомления 'Прибытие в промежуточный пункт' (notice_306)")
    void test_waypointArrivedCommand() {
        approveTripRequestMessageInput.accept(MessageBuilder.withPayload(approveTripRequestMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Вы прибыли в промежуточный пункт, не забудьте отметить чек-бокс геопозиции в вашей заявке для ускорения выплаты компенсации.";
        
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder()
                .waypoints(waypoints)
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .build();
        
        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        List<RequestMessage.Waypoint> waypoints1 = List.of(
                createWaypoint(waypoints.get(0).id(), true, 0),
                createWaypoint(waypoints.get(1).id(), true, 1),
                waypoints.get(2),
                waypoints.get(3)
        );
        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .waypoints(waypoints1)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name());
        
        verifyTokenAndMessage(notificationMessage, tokens);
    }


    @Test
    @DisplayName("Проверка отправки уведомления 'Поездка завершена' (notice_307)")
    void test_tripFinishedCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());
        
        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessages = List.of("По вашей поездке {humanReadableId} завершен этап \"Утверждение маршрута\" со статусом \"{requestStatusDescription}\".",
                "Поездка успешно завершена");
        
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        UUID id1 = UUID.randomUUID();
        var requestMessage = defaultRequestBuilder(id1)
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .waypoints(waypoints)
                .build();
        
        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        UUID id = requestMessage.getId();
        requestMessage = defaultRequestBuilder(id)
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                .waypoints(waypoints)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        
        verifyTokenAndListMessages(notificationMessages, tokens);
    }
    
    @Test
    @DisplayName("Проверка отправки уведомления 'Ожидание выплаты' (notice_310)")
    void test_paymentAwaitingCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Ваша заявка {humanReadableId} была отправлена на выплату.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        UUID id1 = UUID.randomUUID();
        var  requestMessage = defaultRequestBuilder(id1)
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                .waypoints(waypoints)
                .build();
        
        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name())
                .waypoints(waypoints)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }
    
    @Test
    @DisplayName("Проверка отправки уведомления 'Статус ожидания выплаты' (notice_311)")
    void test_paymentStatusCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "По вашей поездке {humanReadableId} завершен этап \"Ожидание выплаты\" со статусом \"{requestStatusDescription}\".";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        UUID id1 = UUID.randomUUID();
        var requestMessage = defaultRequestBuilder(id1)
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name())
                .waypoints(waypoints)
                .build();
        
        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_PAYMENT_DONE.name())
                .waypoints(waypoints)
                .build();
        
        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_PAYMENT_DONE.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }
    
    @Test
    @DisplayName("Проверка отправки уведомления 'Присоединение к совместной поездке' (notice_312)")
    void test_coopTripAttachmentCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "К вашей поездке присоединился {passenger.lastName} {passenger.firstName} " +
                                  "{passenger.patronymic}";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage2).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder(UUID.randomUUID())
                .employeeDriverId(passengerMessage1.getId())
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                .build();
        
        assertThat(requestRepository.count()).isZero();
    
        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        assertThat(requestRepository.count()).isEqualTo(1);
        UUID passengerMessage3 = passengerMessage2.getId();
        requestMessage = defaultRequestBuilder(UUID.randomUUID())
                .passengerId(passengerMessage3)
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                .rideId(requestMessage.getRideId())
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(2);

        verifyTokensAndMessages(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Изменение в совместной поездке' (notice_313)")
    void test_coopTripChangesCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var notificationMessage = "Пассажир {passenger.lastName} {passenger.firstName} {passenger.patronymic} " +
                                  "отказался от совместной поездки с вами № {magentaSharedRequest.magentaId}.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage2).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder(UUID.randomUUID())
                .employeeDriverId(passengerMessage1.getId())
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                .build();
        
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
                                             .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL.name())
                                             .tariffId(tariffMessage.getId())
                                             .transportType(TransportTypeEnum.PERSONAL)
                                             .waypoints(waypointMapper.toEntity(waypoints))
                                             .build();
        requestRepository.save(tripRequest);
        assertThat(requestRepository.count()).isEqualTo(2);

        requestMessage = defaultRequestBuilder(tripRequest.getId())
                .passengerId(tripRequest.getPassengerId())
                .status(TripRequestStatus.PERSONAL_CANCELLED.name())
                .rideId(requestMessage.getRideId())
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        verifyTokenAndMessage(notificationMessage, tokens);
        
    }

    private RequestMessage.RequestMessageBuilder<?, ?> defaultRequestBuilder() {
        return defaultRequestBuilder(UUID.randomUUID());
    }

    private RequestMessage.RequestMessageBuilder<?, ?> defaultRequestBuilder(UUID id) {
        return RequestMessage.builder()
                .id(id)
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
                .tariffId(UUID.randomUUID())
                .tripClass("ECONOMY")
                .transportType(TransportTypeEnum.PERSONAL.name())
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("APPROVED");
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Напоминание о начале поездки' (notice_305)")
    void testTripStartRemindCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Ваша поездка скоро начнется. Не забудьте отметить локации в приложении.";

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder()
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .waypoints(waypoints)
                .build();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        LocalDateTime now =  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_APPROVED.name())
                .waypoints(waypoints)
                .desiredDate(now.plusMinutes(2))
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_APPROVED.name());

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Напоминание о начале поездки' (notice_305) более 15 минут")
    void testTripStartRemindCommandEarly() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        LocalDateTime now =  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        LocalDateTime desiredDate = now.plusMinutes(20);

        var requestMessage = defaultRequestBuilder()
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .waypoints(waypoints)
                .desiredDate(desiredDate)
                .build();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_APPROVED.name())
                .waypoints(waypoints)
                .desiredDate(desiredDate)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_APPROVED.name());

        verify(pushSender, times(0)).send(any(UUID.class), any(), any(), any(),
                any(), any());

        Assertions.assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).hasSize(1);

    }

    @Test
    @DisplayName("Проверка НЕ отправки уведомления 'Напоминание о начале поездки' (notice_305) спустя 15 минут после даты отправления")
    void testTripStartRemindCommandAfter() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        LocalDateTime now =  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        LocalDateTime desiredDate = now.minusMinutes(20);

        var requestMessage = defaultRequestBuilder()
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS.name())
                .waypoints(waypoints)
                .desiredDate(desiredDate)
                .build();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_APPROVED.name())
                .waypoints(waypoints)
                .desiredDate(desiredDate)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_APPROVED.name());

        verify(pushSender, times(0)).send(any(UUID.class), any(), any(), any(),
                any(), any());

        Assertions.assertThat(notificationRepository.findAllBySentIsFalseAndWithTimingSettings()).hasSize(0);

    }

    //notice_308
    @Test
    @DisplayName("Проверка отправки уведомления 'Утверждение финального маршрута на личном транспорте' (notice_308)")
    void test_TRIP_AFFIRM_Command() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessages =
                List.of("Утверждение поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.",
                "Поездка успешно завершена");
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder()
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_TRIP_START_REMIND.name())
                .waypoints(waypoints)
                .build();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL.name())
                .waypoints(waypoints)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo("PERSONAL_AWAITING_TRIP_APPROVAL");

        verifyTokenAndListMessages(notificationMessages, tokens);
    }

    @Test
    @DisplayName("Проверка отправки уведомления 'Статус утверждения финального маршрута на личном транспорте' (notice_309)")
    void testTripAffirmStatusCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "По вашей поездке {humanReadableId} завершен этап \"Утверждение маршрута\" со статусом \"{requestStatusDescription}\".";
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        var tokens = requestMessage.getPassengerId();

        var requestMessage = defaultRequestBuilder()
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL.name())
                .waypoints(waypoints)
                .build();

        assertThat(requestRepository.count()).isZero();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);

        requestMessage = defaultRequestBuilder(requestMessage.getId())
                .approvalState("APPROVED")
                .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                .waypoints(waypoints)
                .build();

        requestMessageInput.accept(MessageBuilder.withPayload(requestMessage).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        var req = requestRepository.findAll().getFirst();
        assertThat(req.getId()).isEqualTo(requestMessage.getId());
        assertThat(req.getStatus()).isEqualTo("PERSONAL_ORDER_PAYMENT_FORMATION");

        verifyTokenAndMessage(notificationMessage, tokens);
    }

    @SuppressWarnings("unchecked")
    public void verifyTokenAndListMessages(List<String> notificationMessage, UUID token) {
        var tokenCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pushSender, times(notificationMessage.size())).send(any(UUID.class),
                tokenCaptor.capture(),
                typeCaptor.capture(),
                templateCaptor.capture(),
                dataCaptor.capture(), additionalDataCaptor.capture());
        var actualTokens = tokenCaptor.getValue();
        var actualMessages = templateCaptor.getAllValues();
        var actualData = dataCaptor.getValue();

        Assertions.assertThat(actualMessages).hasSize(notificationMessage.size());

        Assertions.assertThat(actualMessages).containsExactlyInAnyOrder(notificationMessage.toArray(String[]::new));

        Assertions.assertThat(actualData).isNull();

        var actualToken = (UUID) actualTokens.getFirst();

        Assertions.assertThat(actualToken).isEqualTo(token);
    }

}
