package ru.sber.transport.telemechanic.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.telemechanic.FirstTitleCreateResponseMessage;
import ru.sber.transport.telemechanic.messaging.sender.FirstTitleCreateRequestSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FirstTitleCreateRequestSenderImpl implements FirstTitleCreateRequestSender {

    @Qualifier("firstTitleCreateResponseOutput")
    private final ObjectProvider<OutputBridge> firstTitleCreateResponseOutput;
    @Qualifier("firstTitleCreateResponseOutputSsl")
    private final ObjectProvider<OutputBridge> firstTitleCreateResponseOutputSsl;

    @Override
    public void send(FirstTitleCreateResponseMessage message) {
        Optional.ofNullable(firstTitleCreateResponseOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
        Optional.ofNullable(firstTitleCreateResponseOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }
}
