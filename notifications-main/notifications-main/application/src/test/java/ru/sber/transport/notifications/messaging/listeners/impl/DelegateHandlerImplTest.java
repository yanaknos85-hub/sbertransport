package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.DelegateRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@EmbeddedPostgres
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class DelegateHandlerImplTest {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DelegateRepository delegateRepository;
    
    private final Employee employeeDelegate = new Employee();
    
    private final Employee employeeSupervisor = new Employee();

    @Autowired
    @Qualifier("delegateInput")
    private Consumer<Message<DelegateMessage>> delegateMessageInput;


    @BeforeEach
    void setUp() {
        employeeDelegate.setId(UUID.randomUUID());
        employeeDelegate.setFirstName("Delegate firstname");
        employeeDelegate.setLastName("Delegate lastName");
        employeeDelegate.setPatronymic("Delegate patronymic");
        employeeRepository.save(employeeDelegate);
        employeeSupervisor.setId(UUID.randomUUID());
        employeeSupervisor.setFirstName("Supervisor firstname");
        employeeSupervisor.setLastName("Supervisor lastName");
        employeeSupervisor.setPatronymic("Supervisor patronymic");
        employeeRepository.save(employeeSupervisor);
    }
   
    @Test
    @DisplayName("Новая запись")
    void test_new() {
        var message =  DelegateMessage.builder()
                                         .id(UUID.randomUUID())
                                         .delegateId(employeeDelegate.getId())
                                         .supervisorId(employeeSupervisor.getId())
                                         .startDate(LocalDate.now().plusDays(5))
                                         .endDate(LocalDate.now().plusMonths(4).plusDays(5))
                                         .transportTypeId(TransportTypeEnum.BICYCLE.getId())
                                         .deleted(false)
                                         .build();

        delegateMessageInput.accept(MessageBuilder.withPayload(message).build());

        var delegate = delegateRepository.findById(message.getId()).get();
        
        assertThat(delegate)
                .matches(del -> del.getId().equals(message.getId()))
                .matches(del -> del.getSupervisorId().equals(employeeSupervisor.getId()))
                .matches(del -> del.getDelegateId().equals(employeeDelegate.getId()))
                .matches(del -> del.getStartDate().equals(message.getStartDate()))
                .matches(del -> del.getEndDate().equals(message.getEndDate()))
                .matches(del -> del.getTransportType().getId().equals(message.getTransportTypeId()));
    }
    
    @DisplayName("Изменение")
    @Test
    void test_edit() {
        var message =  DelegateMessage.builder()
                                       .id(UUID.randomUUID())
                                       .delegateId(employeeDelegate.getId())
                                       .supervisorId(employeeSupervisor.getId())
                                       .startDate(LocalDate.now().plusDays(5))
                                       .endDate(LocalDate.now().plusMonths(4).plusDays(5))
                                       .transportTypeId(TransportTypeEnum.BICYCLE.getId())
                                       .deleted(false)
                                       .build();

        delegateMessageInput.accept(MessageBuilder.withPayload(message).build());

        message.setTransportTypeId(TransportTypeEnum.TAXI.getId());

        delegateMessageInput.accept(MessageBuilder.withPayload(message).build());

        var delegate = delegateRepository.findById(message.getId()).get();
    
        assertThat(delegate)
                .matches(del -> del.getId().equals(message.getId()))
                .matches(del -> del.getSupervisorId().equals(employeeSupervisor.getId()))
                .matches(del -> del.getDelegateId().equals(employeeDelegate.getId()))
                .matches(del -> del.getStartDate().equals(message.getStartDate()))
                .matches(del -> del.getEndDate().equals(message.getEndDate()))
                .matches(del -> del.getTransportType().getId().equals(message.getTransportTypeId()));
    }
}