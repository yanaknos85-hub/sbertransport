package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RequestPayoutSenderImpl implements RequestPayoutSender {

    @Qualifier("requestPayoutOutput")
    private final ObjectProvider<OutputBridge> requestPayoutOutput;

    @Override
    public void send(RequestPayoutMessage message) {
        Optional.ofNullable(requestPayoutOutput.getIfAvailable())
                .ifPresent(outputBridge -> outputBridge.send(message));
    }
}
