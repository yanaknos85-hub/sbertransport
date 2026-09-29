package ru.sberbank.ditsib.transport.vehicle.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;
import ru.sberbank.ditsib.transport.vehicle.providers.DepartmentProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.EmployeeProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.OrganizationProvider;
import ru.sberbank.ditsib.transport.vehicle.providers.PositionProvider;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;

import java.util.function.Consumer;

/**
 * Конфигурация получателей.
 */
@Slf4j
@Configuration
public class ListenersConfig {
    
    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInputSsl(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }

    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInputDlq(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInputDlqSsl(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }

    @NotNull
    private Consumer<Message<OrganizationMessage>> getOrganizationConsumer(OrganizationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInputSsl(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }

    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInputDlq(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInputDlqSsl(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }

    @NotNull
    private Consumer<Message<DepartmentMessage>> getDepartmentConsumer(DepartmentProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @Bean
    Consumer<Message<PositionMessage>> positionsInput(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    Consumer<Message<PositionMessage>> positionsInputSsl(PositionProvider provider) {
        return getPositionConsumer(provider);
    }

    @Bean
    Consumer<Message<PositionMessage>> positionsInputDlq(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    Consumer<Message<PositionMessage>> positionsInputDlqSsl(PositionProvider provider) {
        return getPositionConsumer(provider);
    }

    @NotNull
    private Consumer<Message<PositionMessage>> getPositionConsumer(PositionProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    Consumer<Message<EmployeeMessage>> employeesInputSsl(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInputDlq(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    Consumer<Message<EmployeeMessage>> employeesInputDlqSsl(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }

    @NotNull
    private Consumer<Message<EmployeeMessage>> getEmployeeConsumer(EmployeeProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }

    @Bean
    Consumer<Message<OdometerHistoryValueMessage>> odometerHistoryValueInput(TransportService transportService) {
        return message -> {
            var payload = message.getPayload();
            transportService.addIndicatorsHistory(payload);
        };
    }
    
    @Bean
    Consumer<Message<OdometerHistoryValueMessage>> odometerHistoryValueInputSsl(TransportService transportService) {
        return message -> {
            var payload = message.getPayload();
            transportService.addIndicatorsHistory(payload);
        };
    }

}
