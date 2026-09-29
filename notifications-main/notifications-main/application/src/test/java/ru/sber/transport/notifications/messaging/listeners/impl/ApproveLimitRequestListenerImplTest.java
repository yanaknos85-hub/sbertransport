package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.messaging.ApproveLimitRequestMessage;
import ru.sber.transport.notifications.database.dao.messages.limits.ApproveLimitRequestRepository;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.LimitType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("java:S5961")
@SpringBootTest
@DisplayName("Проверка слушателя сообщений заявок на лимит")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class ApproveLimitRequestListenerImplTest {
    
    @Autowired
    private ApproveLimitRequestRepository repository;

    @MockitoBean
    private Processor<ApproveLimitRequest> processor;

    @Autowired
    private Consumer<Message<ApproveLimitRequestMessage>> approveLimitsRequestInput;

    @Test
    @DisplayName("Новая запись")
    void test_new_edit_approve() throws JsonProcessingException {
        var message = new ApproveLimitRequestMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HRR",
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(3),
                "TAXI",
                2021,
                10,
                "INIT",
                LimitType.Values.DEPARTMENT,
                1111L,
                "desc",
                "o-la-la!",
                20000L,
                ApprovalState.APPROVED.name(),
                LocalDateTime.now().minusDays(2),
                false      
        );
        
        assertThat(repository.count()).isZero();
        approveLimitsRequestInput.accept(MessageBuilder.withPayload(message).build());

        var argumentCaptor = ArgumentCaptor.forClass(ApproveLimitRequest.class);
        Mockito.verify(processor).process(argumentCaptor.capture());
        var actual = argumentCaptor.getValue();
        assertThat(repository.count()).isEqualTo(1);
        
        Assertions.assertThat(actual.getId()).isEqualTo(message.getId());
        Assertions.assertThat(actual.getApprovalDate()).isEqualTo(message.approvalDate());
        Assertions.assertThat(actual.getApprovalState().name()).isEqualTo(message.approvalState());
        Assertions.assertThat(actual.getCreationTime()).isEqualTo(message.creationTime());
        Assertions.assertThat(actual.getStatus()).isEqualTo(message.status());
        Assertions.assertThat(actual.getLimitType().getName()).isEqualTo(message.limitType());
        Assertions.assertThat(actual.getAuthorId()).isEqualTo(message.authorId());
        Assertions.assertThat(actual.getEmployeeId()).isEqualTo(message.departmentHeadId());
        Assertions.assertThat(actual.getDeclineReason()).isEqualTo(message.declineReason());
        Assertions.assertThat((long)actual.getSum()).isEqualTo(message.sum());
        Assertions.assertThat(actual.getDescription()).isEqualTo(message.description());
        Assertions.assertThat(actual.getHumanReadableId()).isEqualTo(message.humanReadableId());
        Assertions.assertThat(actual.getLimitRequestId()).isEqualTo(message.limitRequestId());
        Assertions.assertThat(actual.getDepartmentId()).isEqualTo(message.departmentId());
        Assertions.assertThat(actual.getOrganizationId()).isEqualTo(message.organizationDepartmentId());
        Assertions.assertThat(actual.getPeriod()).isEqualTo(message.period());
        Assertions.assertThat(actual.getSumLimit()).isEqualTo(message.sumLimit());
        Assertions.assertThat(actual.getYear()).isEqualTo(message.year());
        Assertions.assertThat(actual.isDeleted()).isFalse();
        
        // вариант - для сотрудника
        message = new ApproveLimitRequestMessage(
                message.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HRR",
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(3),
                "TAXI",
                2021,
                10,
                "INIT",
                LimitType.Values.EMPLOYEE,
                1111L,
                "desc",
                "o-la-la!",
                20000L,
                ApprovalState.APPROVED.name(),
                LocalDateTime.now().minusDays(2),
                false
        );

        approveLimitsRequestInput.accept(MessageBuilder.withPayload(message).build());

        argumentCaptor = ArgumentCaptor.forClass(ApproveLimitRequest.class);
        Mockito.verify(processor, Mockito.times(2)).process(argumentCaptor.capture());
        List<ApproveLimitRequest> allValues = argumentCaptor.getAllValues();
        assertThat(repository.count()).isEqualTo(1);
        
        Assertions.assertThat(allValues.get(1).getEmployeeId()).isEqualTo(message.employeeId());
        Assertions.assertThat(allValues.get(1).getOrganizationId()).isEqualTo(message.organizationEmployeeId());
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var message = new ApproveLimitRequestMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HRR",
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(3),
                "TAXI",
                2021,
                10,
                "INIT",
                LimitType.Values.EMPLOYEE,
                1111L,
                "desc",
                "o-la-la!",
                20000L,
                ApprovalState.APPROVED.name(),
                LocalDateTime.now().minusDays(2),
                false
        );
        UUID messageId = message.getId();

        assertThat(repository.count()).isZero();
        approveLimitsRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isEqualTo(1);

        message = new ApproveLimitRequestMessage(
                messageId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HRR",
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(3),
                "TAXI",
                2021,
                10,
                "INIT",
                LimitType.Values.EMPLOYEE,
                1111L,
                "desc",
                "o-la-la!",
                20000L,
                ApprovalState.APPROVED.name(),
                LocalDateTime.now().minusDays(2),
                true
        );

        approveLimitsRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(repository.count()).isZero();
    }
}