package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.mappers.DispatcherMapper;
import ru.sber.transport.dispatcher.messaging.senders.DispatcherSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.Map;

/**
 * Реализация отправителя данных.
 */
@Component
@RequiredArgsConstructor
class DispatcherSenderImpl implements DispatcherSender {

    @Qualifier("usersOutput")
    private final ObjectProvider<OutputBridge> usersOutput;

    @Qualifier("usersOutputSsl")
    private final ObjectProvider<OutputBridge> usersOutputSsl;

    @Qualifier("dispatcherOutput")
    private final ObjectProvider<OutputBridge> dispatcherOutput;

    @Qualifier("dispatcherOutputSsl")
    private final ObjectProvider<OutputBridge> dispatcherOutputSsl;

    private final DispatcherMapper dispatcherMapper;

    @Override
    public void send(Dispatcher dispatcher) {
        var dispatcherMessage = dispatcherMapper.toMessage(dispatcher);
        dispatcherOutput.ifAvailable(ob -> ob.send(dispatcherMessage));
        dispatcherOutputSsl.ifAvailable(ob -> ob.send(dispatcherMessage));
        if (dispatcher.getOauthId() == null) {
            var active = dispatcher.isActive();
            var userMessage = UserMessage.builder()
                    .id(dispatcher.getId())
                    .deleted(!active)
                    .active(active)
                    .orgStructureType("EXTERNAL")
                    .email(dispatcher.getEmail())
                    .phone(dispatcher.getPhone())
                    .lastName(dispatcher.getLastName())
                    .firstName(dispatcher.getFirstName())
                    .patronymic(dispatcher.getPatronymic())
                    .scope(UserMessage.Scope.DISPATCHER)
                    .build();
            usersOutput.ifAvailable(ob -> ob.send(userMessage, Map.of(UserMessage.TYPE, userMessage.orgStructureType())));
            usersOutputSsl.ifAvailable(ob -> ob.send(userMessage, Map.of(UserMessage.TYPE, userMessage.orgStructureType())));
        }
    }
}
