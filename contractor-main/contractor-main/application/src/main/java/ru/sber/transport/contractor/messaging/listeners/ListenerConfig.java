package ru.sber.transport.contractor.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.database.model.*;
import ru.sber.transport.contractor.mappers.EmployeeMapper;
import ru.sber.transport.contractor.service.*;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Конфигурация слушателей брокера.
 */
@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        return employeesInputSsl(employeeService, employeeMapper);
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInputSsl(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = message.getId();
            if (id == null) {
                id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            }

            var employee = employeeService.get(id).orElseGet(Employee::new);
            employeeMapper.update(employee, message);
            employeeService.save(employee);
        };
    }

}
