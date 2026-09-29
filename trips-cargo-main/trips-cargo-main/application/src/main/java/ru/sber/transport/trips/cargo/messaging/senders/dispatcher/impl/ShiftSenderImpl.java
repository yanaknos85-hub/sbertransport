package ru.sber.transport.trips.cargo.messaging.senders.dispatcher.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShiftSenderImpl implements ShiftSender {

    @Qualifier("shiftOutput")
    private final ObjectProvider<OutputBridge> shiftOutput;

    @Qualifier("shiftOutputSsl")
    private final ObjectProvider<OutputBridge> shiftOutputSsl;

    private final ShiftMapper shiftMapper;

    @Override
    public void send(Shift shift) {
        var message = shiftMapper.toMessage(shift);
        shiftOutput.ifAvailable(ob -> ob.send(message, Map.of("source", Source.TRIPS_CARGO.name())));
        shiftOutputSsl.ifAvailable(ob -> ob.send(message, Map.of("source", Source.TRIPS_CARGO.name())));
    }
}
