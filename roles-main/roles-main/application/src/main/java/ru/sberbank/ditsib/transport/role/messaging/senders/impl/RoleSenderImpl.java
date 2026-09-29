package ru.sberbank.ditsib.transport.role.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sberbank.ditsib.transport.role.messaging.mappers.RoleMessageMapper;
import ru.sberbank.ditsib.transport.role.messaging.senders.RoleSender;

/**
 * Реализация отправителя ролей.
 */
@Component
@RequiredArgsConstructor
class RoleSenderImpl implements RoleSender {

    @Qualifier("rolesOutput")
    private final ObjectProvider<OutputBridge> rolesOutput;

    @Qualifier("rolesOutputSsl")
    private final ObjectProvider<OutputBridge> rolesOutputSsl;

    private final RoleMessageMapper mapper;
    
    @Override
    public void send(RoleRecord entity, boolean deleted) {
        var message = mapper.toMessage(entity, deleted);
        rolesOutput.ifAvailable(ob -> ob.send(message));
        rolesOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
