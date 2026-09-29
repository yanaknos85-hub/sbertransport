package ru.sber.transport.address.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.address.business.use_cases.Employees;
import ru.sber.transport.address.business.model.*;
import ru.sber.transport.address.business.use_cases.Frequentlies;
import ru.sber.transport.address.messaging.mapper.AddressMessageMapper;
import ru.sber.transport.address.messaging.mapper.EmployeeMessageMapper;
import ru.sber.transport.messages.addresses.avro.UsedAddressMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<Map<String, Object>>> userAddressInput(Frequentlies frequentlies, ObjectMapper mapper) {
        return userAddressInputSsl(frequentlies, mapper);
    }

    @Bean
    Consumer<Message<Map<String, Object>>> userAddressInputSsl(Frequentlies frequentlies, ObjectMapper mapper) {
        return message -> {
            var payload = message.getPayload();
            var address = mapper.convertValue(payload, GeoAddress.class);
            var first = Boolean.TRUE.equals(payload.getOrDefault("first", false));
            var employeeId = Optional.ofNullable(payload.get("employeeId")).map(String::valueOf).map(UUID::fromString).orElse(null);
            if (employeeId != null) {
                try {
                    frequentlies.increaseUsage(address, first, employeeId);
                } catch (Exception e) {
                    log.error("ERROR!", e);
                }
            }
        };
    }

    @Bean
    Consumer<Message<UsedAddressMessage>> userAddressInputAvro(Frequentlies frequentlies, AddressMessageMapper mapper) {
        return message -> {
            var payload = message.getPayload();
            frequentlies.increaseUsage(mapper.toBusiness(payload), payload.getFirst(), payload.getEmployeeId());
        };
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeeInput(Employees employees, EmployeeMessageMapper mapper) {
        return raw -> employees.update(mapper.toBusiness(raw.getPayload()));
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeeInputSsl(Employees employees, EmployeeMessageMapper mapper) {
        return employeeInput(employees, mapper);
    }

    @Bean
    Consumer<Message<ru.sber.transport.messages.corporate.avro.EmployeeMessage>> employeeInputAvro(Employees employees, EmployeeMessageMapper mapper) {
        return raw -> employees.update(mapper.toBusiness(raw.getPayload()));
    }

}
