package ru.sber.transport.request.external.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;
import ru.sber.transport.request.external.messaging.senders.FraudMonitoringSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudMonitoringSenderImpl implements FraudMonitoringSender {

    @Qualifier("fraudMonitoringOutput")
    private final ObjectProvider<OutputBridge> fraudMonitoringOutput;

    @Override
    public void send(FraudMonitoringMessage message) {
        log.info("Sending fraud monitoring message: requestId={}, fraudData={}", message.id(), message.fraudData());
        fraudMonitoringOutput.ifAvailable(outputBridge -> outputBridge.send(message));
        log.info("Fraud monitoring message sent successfully: requestId={}", message.id());
    }
}
