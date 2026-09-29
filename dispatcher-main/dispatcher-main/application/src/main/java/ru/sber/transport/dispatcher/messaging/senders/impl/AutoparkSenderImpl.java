package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.mappers.AutoparkMapper;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.dispatcher.messaging.senders.AutoparkSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoparkSenderImpl implements AutoparkSender {

    @Qualifier("autoparkOutput")
    private final ObjectProvider<OutputBridge> autoparkOutput;

    @Qualifier("autoparkOutputSsl")
    private final ObjectProvider<OutputBridge> autoparkOutputSsl;

    private final AutoparkMapper autoparkMapper;

    @Override
    public void send(Autopark autopark) {
        var message = autoparkMapper.toAutoparkMessage(autopark);
        autoparkOutput.ifAvailable(ob -> ob.send(message));
        autoparkOutputSsl.ifAvailable(ob -> ob.send(message));
    }

    @Override
    public void sendAll(List<Autopark> autoparks) {
        autoparks.forEach(this::send);
    }
}
