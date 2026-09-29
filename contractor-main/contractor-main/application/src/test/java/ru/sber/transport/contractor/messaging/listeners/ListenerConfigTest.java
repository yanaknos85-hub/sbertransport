package ru.sber.transport.contractor.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.database.model.*;
import ru.sber.transport.contractor.mappers.*;
import ru.sber.transport.contractor.messages.DriverMessage;
import ru.sber.transport.contractor.messages.ShiftMessage;
import ru.sber.transport.contractor.messages.Source;
import ru.sber.transport.contractor.service.EmployeeService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@DisplayName("Проверка получения данных")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @DisplayName("Получение сотрудников")
    @Test
    void test_employee() {
        var employeeService = mock(EmployeeService.class);
        var employeeMapper = new EmployeeMapperImpl();

        var message = Instancio.of(EmployeeMessage.class)
                .create();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.employeesInput(employeeService, employeeMapper);

        input.accept(rawMessage);

        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeService).save(employeeCaptor.capture());

        var saved = employeeCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(message.getId());
        assertThat(saved.getFirstName()).isEqualTo(message.getFirstName());
        assertThat(saved.getLastName()).isEqualTo(message.getLastName());
        assertThat(saved.getPatronymic()).isEqualTo(message.getPatronymic());
        assertThat(saved.getUserId()).isEqualTo(message.getUserId());
        assertThat(saved.getOrganizationId()).isEqualTo(message.getOrganizationId());
    }

}