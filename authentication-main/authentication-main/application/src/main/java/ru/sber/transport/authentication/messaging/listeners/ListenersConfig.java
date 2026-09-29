package ru.sber.transport.authentication.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.kafka.BinderHeaderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.messaging.listeners.providers.RoleProvider;
import ru.sber.transport.authentication.messaging.mappers.AccountMessageMapper;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserRoleMessage;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Конфигурация получателей.
 */
@Slf4j
@Configuration
class ListenersConfig {
    
    @Bean
    Consumer<Message<List<UserMessage>>> usersInput(AccountCases accountCases, AccountMessageMapper mapper) {
        return message -> {
            var headersList = ReflectionUtils.castObjectToList(message.getHeaders().get(KafkaHeaders.BATCH_CONVERTED_HEADERS), Map.class);
            var payload = message.getPayload();

            var deactivateList = new LinkedList<UUID>();
            var saveList = new LinkedList<AccountDto>();

            for (var index = 0; index < payload.size(); index++) {
                var headers = headersList.get(index);

                var headerValue = headers.get("type");
                var type = defineValue(headerValue);

                if (Objects.equals("EXTERNAL", type)) {
                    var payloadItem = payload.get(index);
                    if (payloadItem.deleted() || !payloadItem.active()) {
                        deactivateList.add(payloadItem.getId());
                    } else {
                        saveList.add(mapper.toBusiness(payloadItem));
                    }
                }
            }
            if (!deactivateList.isEmpty()) {
                accountCases.deactivate(deactivateList);
            }
            if (!saveList.isEmpty()) {
                accountCases.save(saveList);
            }
        };
    }

    private String defineValue(Object headerValue) {
        if (headerValue instanceof DefaultKafkaHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"","");
        } else if (headerValue instanceof BinderHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"", "");
        } else {
            return String.valueOf(headerValue);
        }
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInput(RoleProvider roleProvider) {
        return message -> {
            var headers = message.getHeaders();
            var payload = message.getPayload();
            var code = String.valueOf(headers.getOrDefault(KafkaHeaders.RECEIVED_KEY, payload.code()));
            
            var deleted = Boolean.TRUE.equals(payload.deleted());
    
            if (deleted) {
                roleProvider.delete(code);
            } else if (payload.exclusives() != null && payload.exclusives().contains("EXTERNAL")) {
                roleProvider.save(payload);
            }
        };
    }
    
    @Bean
    Consumer<Message<UserRoleMessage>> userRolesInput(AccountCases accountCases) {
        return message -> {
            var payload = message.getPayload();
            try {
                accountCases.setRoles(payload.getUser(), payload.getRoles());
            } catch (AccountNotFoundException e) {
                log.info("Setting roles failed", e);
            }
        };
    }
}
