package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.ExpectedData;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@AutoConfigureMockMvc
@DisplayName("Проверка слушателя сообщений и согласованиях")
@MockitoBean(types = PushSender.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
@EmbeddedPostgres
@Transactional
class TripApproveListenerImplTest {

    @Autowired
    private TripApproveRepository approveRepository;

    @MockitoBean
    private PushSender pushSender;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private TripRequestRepository requestRepository;

    @Autowired
    private Consumer<Message<ApproveTripRequestMessage>> tripApproveInput;

    @DisplayName("Новый")
    @Test
    void test_new() {
        var message = new ApproveTripRequestMessage(
                UUID.randomUUID(),
                true,
                UUID.randomUUID(),
                null,
                null
        );

        assertThat(approveRepository.count()).isZero();
        tripApproveInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(approveRepository.count()).isEqualTo(1);

        var actual = approveRepository.findAll().getFirst();
        assertThat(actual.getApproverId()).isEqualTo(message.actorEmployeeId());
        assertThat(actual.getRequestId()).isEqualTo(message.actionId());
        assertThat(actual.getStatus()).isTrue();
    }

    @Test
    @DisplayName("Проверка отправки уведомления для перехода в приложение по каршерингу")
    void test_sendCarsharingNotification() {
        var message = new ApproveTripRequestMessage(
                UUID.randomUUID(),
                true,
                UUID.randomUUID(),
                null,
                null
        );

        var requestMessage = TripRequest.builder()
                .id(message.actionId())
                .purposeId(UUID.randomUUID())
                .approvalId(null)
                .authorId(UUID.randomUUID())
                .coopTrip(false)
                .commentForDriver("Comment")
                .creationTime(LocalDateTime.now())
                .desiredDate(LocalDateTime.now().plusDays(1))
                .expected(ExpectedData.builder().cost(100D).time(Duration.ZERO)
                        .distance(200D).build())
                .finishedTime(LocalDateTime.now().plusDays(2))
                .humanReadableId("HRI")
                .passengerCount(3)
                .passengerId(UUID.randomUUID())
                .status(TripRequestStatus.CARSHARING_APPROVED.name())
                .approvalState("APPROVED")
                .tariffId(UUID.randomUUID())
                .transportType(TransportTypeEnum.CARSHARING)
                .build();

        var tokens = new ArrayList<UUID>();
        var count = 5;
        for (var i = 0; i < count; i++) {
            tokens.add(requestMessage.getPassengerId());
        }

        when(requestRepository.findById(any())).thenReturn(Optional.of(requestMessage));
        when(employeeService.get(any())).thenReturn(Optional.of(Employee.builder().id(requestMessage.getPassengerId()).build()));

        var tokenCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);

        tripApproveInput.accept(MessageBuilder.withPayload(message).build());
        verify(pushSender).send(any(UUID.class), tokenCaptor.capture(), any(), messageCaptor.capture(), any(), any());

        var actualTokens = (List<UUID>) tokenCaptor.getAllValues().stream().flatMap(Collection::stream).toList();
        var actualMessages = messageCaptor.getAllValues();

        assertThat(actualTokens).hasSize(1);
        assertThat(actualMessages).hasSize(1);

        for (var i = 0; i < actualTokens.size(); i++) {
            var actualToken = actualTokens.get(i);
            var actualMessage = actualMessages.getFirst();

            assertThat(actualToken).isEqualTo(tokens.get(i));
            assertThat(actualMessage).isEqualTo("Перейдите в приложение для каршеринга");
        }
    }

}