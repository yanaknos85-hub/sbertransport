package ru.sber.transport.notifications.commands;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@AutoConfigureMockMvc
@DisplayName("Проверка отправки уведомлений заявок на общественный транспорт")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class PublicCommandsTest extends SharedCommands {
    
    @Autowired
    private TripRequestRepository requestRepository;
    
    @Autowired
    private TripApproveRepository approveRepository;
    
    @Test
    @DisplayName("Проверка отправки уведомления 'Согласование заявки' (notice_201)")
    void test_requestApprovingCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());
        deadlineSettingsMessageInput.accept(MessageBuilder.withPayload(deadlineMessage).build());
       
        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.";
        
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

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
                .status(TripRequestStatus.PUBLIC_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.PUBLIC.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
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
    @WithMockUser(roles = "GUEST")
    @DisplayName("Проверка отправки уведомления 'Статус согласования заявки' (notice_202)")
    void test_requestApproveStatusCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());

        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".";
        
        employeeMessageInput.accept(MessageBuilder.withPayload(passengerMessage1).build());

        employeeMessageInput.accept(MessageBuilder.withPayload(approverEmployeeMessage).build());

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
                .status(TripRequestStatus.PUBLIC_AWAITING_APPROVAL.name())
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.PUBLIC.name())
                .tripClass("ECONOMY")
                .waypoints(waypoints)
                .rideId(UUID.randomUUID())
                .approvalState("NEW")
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
}
