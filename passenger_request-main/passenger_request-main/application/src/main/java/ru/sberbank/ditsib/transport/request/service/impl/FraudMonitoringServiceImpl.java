package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.messaging.message.FraudMonitoringMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.FraudMonitoringSender;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudMonitoringServiceImpl implements FraudMonitoringService {

    private static final String FRAUD_SOURCE = "request";
    private final FraudMonitoringSender fraudMonitoringSender;

    @Override
    public void send(UUID requestId, FraudType type, String comment) {
        fraudMonitoringSender.send(new FraudMonitoringMessage(
                requestId,
                FRAUD_SOURCE,
                List.of(new FraudMonitoringMessage.FraudDataItem(
                        type != null ? type.name() : null,
                        comment,
                        null
                ))
        ));
    }
}
