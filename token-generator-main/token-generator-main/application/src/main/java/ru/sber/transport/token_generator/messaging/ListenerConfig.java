package ru.sber.transport.token_generator.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;
import ru.sber.transport.token_generator.messaging.mappers.RolesMapper;
import ru.sber.transport.token_generator.messaging.providers.RolesProvider;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;

import java.util.function.Consumer;

@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<RoleMessage>> rolesInput(RolesProvider rolesProvider, RolesMapper mapper) {
        return rolesInputSsl(rolesProvider, mapper);
    }

    @Bean
    Consumer<Message<AccountMessage>> accountsInput(AccountsProvider accountsProvider) {
        return accountsInputSsl(accountsProvider);
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInputSsl(RolesProvider rolesProvider, RolesMapper mapper) {
        return rawMessage -> {
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, String.class);
            var message = rawMessage.getPayload();
            if (Boolean.FALSE.equals(message.deleted())) {
                var role = rolesProvider.get(id).orElseGet(RolesRecord::new);
                mapper.update(role, message);
                rolesProvider.save(role);
            } else {
                rolesProvider.delete(id);
            }
        };
    }

    @Bean
    Consumer<Message<AccountMessage>> accountsInputSsl(AccountsProvider accountsProvider) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = (String) rawMessage.getHeaders().getOrDefault(KafkaHeaders.RECEIVED_KEY, message.getId());
            if (message.active()) {
                accountsProvider.save(id, message);
            } else {
                accountsProvider.delete(id);
            }
        };
    }

}
