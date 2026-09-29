package ru.sber.transport.request.external.messaging.senders.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.external.messaging.senders.RequestPayoutSender;

@RequiredArgsConstructor
public class RequestPayoutSenderImpl implements RequestPayoutSender {

    private final ObjectProvider<OutputBridge> requestPayoutOutput;
    private final ObjectProvider<OutputBridge> requestPayoutOutputSsl;


    @Override
    public void send(RequestPayoutMessage message) {
        Optional.ofNullable(requestPayoutOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
        Optional.ofNullable(requestPayoutOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }
}
