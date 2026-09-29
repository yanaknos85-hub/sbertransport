package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.messaging.LimitMessage;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.limits.LimitRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Требуется переработка")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class LimitBalanceListenerImplTest {

    @Autowired
    private  LimitRepository limitRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @MockitoBean
    private Processor<Limit> processor;
   
    private final UUID UUID_LIMIT = UUID.randomUUID();
    private final UUID UUID_ORGANIZATION = UUID.randomUUID();
    private final Employee employee = Employee.builder().id(UUID.randomUUID()).build();

    @Autowired
    @Qualifier("limitsInput")
    private Consumer<Message<LimitMessage>> limitMessageInput;

    @BeforeEach
    void init(){
        employeeRepository.save(employee);
    }
    
    @Test
    @DisplayName("Новая запись")
    void test_new() throws JsonProcessingException {
     
        var messageDepartment = new LimitMessage(
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null,
                null,
                employee.getId(),
                UUID_ORGANIZATION,
                null,
                null,
                null,
                100000L,
                1010L,
                null,
                "TAXI",
                "DEPARTMENT",
                "SHARED",
                null,
                null,
                "MONTH",
                null,
                "LIMIT_CHANGE_BALANCE",
                false,
                null,
                null,
                UUID_LIMIT,
                true,
                false
        );

        assertThat(limitRepository.count()).isZero();

        limitMessageInput.accept(MessageBuilder.withPayload(messageDepartment).build());

        var argumentCaptor = ArgumentCaptor.forClass(Limit.class);
        Mockito.verify(processor).process(argumentCaptor.capture());
        var actual = argumentCaptor.getValue();
        assertThat(limitRepository.count()).isEqualTo(1);
        assertRequests(actual, messageDepartment);
        assertThat(actual.getBalance()).isEqualTo(messageDepartment.balance());
    }
    
    @Test
    @DisplayName("Новая запись - первичный лимит без данных по балансу")
    void test_new_primary() throws JsonProcessingException {
        
        // Данные по "первичному" лимиту без баланса
        var messageDepartment = new LimitMessage(
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null,
                null,
                employee.getId(),
                UUID_ORGANIZATION,
                null,
                null,
                null,
                100000L,
                null,
                null,
                "TAXI",
                "DEPARTMENT",
                "SHARED",
                null,
                null,
                "MONTH",
                null,
                "NEW_LIMIT",
                false,
                null,
                null,
                UUID_LIMIT,
                true,
                false
        );

        limitMessageInput.accept(MessageBuilder.withPayload(messageDepartment).build());

        var argumentCaptor = ArgumentCaptor.forClass(Limit.class);
        Mockito.verify(processor).process(argumentCaptor.capture());
        var actual = argumentCaptor.getValue();
        assertThat(limitRepository.count()).isEqualTo(1);
        assertRequests(actual, messageDepartment);
        assertThat(actual.getBalance()).isEqualTo(messageDepartment.sum());
    }

    @DisplayName("Изменение")
    @Test
    void test_edit() throws JsonProcessingException {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee = employeeRepository.save(employee);

        var message = new LimitMessage(
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null,
                null,
                employee.getId(),
                UUID_ORGANIZATION,
                null,
                null,
                null,
                100000L,
                null,
                null,
                "TAXI",
                "EMPLOYEE",
                "SHARED",
                null,
                null,
                "MONTH",
                null,
                "LIMIT_CHANGE_BALANCE",
                false,
                null,
                null,
                UUID_LIMIT,
                true,
                false
        );

        assertThat(limitRepository.count()).isZero();

        limitMessageInput.accept(MessageBuilder.withPayload(message).build());

        var argumentCaptor = ArgumentCaptor.forClass(Limit.class);
        Mockito.verify(processor).process(argumentCaptor.capture());
        var actual = argumentCaptor.getValue();
    
        assertThat(limitRepository.count()).isEqualTo(1);
        assertRequests(actual, message);
     
        UUID id = message.getId();

        var messageNew = new LimitMessage(
                id,
                LocalDateTime.now(),
                null,
                null,
                null,
                employee.getId(),
                UUID_ORGANIZATION,
                null,
                null,
                null,
                200000L,
                333L,
                null,
                "TAXI",
                "EMPLOYEE",
                "SHARED",
                null,
                null,
                "MONTH",
                null,
                "LIMIT_CHANGE_SUM",
                false,
                null,
                null,
                UUID_LIMIT,
                true,
                false
        );

        limitMessageInput.accept(MessageBuilder.withPayload(messageNew).build());

        assertThat(limitRepository.count()).isEqualTo(1);
        assertRequests(limitRepository.findAll().getFirst(), messageNew);
    }
    
    private void assertRequests(Limit actual, LimitMessage expected) {
        assertThat(actual.getSum()).isEqualTo(expected.sum());
        assertThat(actual.getOwnerId()).isEqualTo(expected.ownerId());
        assertThat(actual.getLimitStatus()).isEqualTo(expected.limitStatus());
        assertThat(actual.getOrganizationId()).isEqualTo(expected.organizationId());
        assertThat(actual.getTransportType()).isEqualTo(expected.transportType());
        assertThat(actual.getLimitId()).isEqualTo(expected.limitId());
        assertThat(actual.getLimitType()).isEqualTo(expected.limitType());
        assertThat(actual.getPeriodNumber()).isEqualTo(Optional.ofNullable(expected.periodNumber()).orElse(0));
        assertThat(actual.getLimitSharingType()).isEqualTo(expected.limitSharingType());
    }
}