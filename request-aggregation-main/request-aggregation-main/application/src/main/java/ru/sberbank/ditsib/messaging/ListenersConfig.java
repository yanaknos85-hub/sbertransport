package ru.sberbank.ditsib.messaging;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.provider.DepartmentProvider;
import ru.sberbank.ditsib.provider.EmployeeProvider;
import ru.sberbank.ditsib.provider.OrganizationProvider;
import ru.sberbank.ditsib.provider.PositionProvider;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

/**
 * Конфигурация получателей.
 */
@Configuration
public class ListenersConfig {
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputSsl(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputDlq(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputDlqSsl(OrganizationProvider provider) {
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
    public Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputSsl(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputDlq(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputDlqSsl(DepartmentProvider provider) {
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
    public Consumer<Message<PositionMessage>> positionsInput(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputSsl(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputDlq(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputDlqSsl(PositionProvider provider) {
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
    public Consumer<Message<EmployeeMessage>> employeesInput(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputSsl(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputDlq(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputDlqSsl(EmployeeProvider provider) {
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
}
