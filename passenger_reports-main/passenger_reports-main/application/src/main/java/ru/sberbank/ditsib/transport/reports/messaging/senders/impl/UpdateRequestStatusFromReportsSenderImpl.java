package ru.sberbank.ditsib.transport.reports.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.reports.mappers.UpdateRequestStatusMapper;
import ru.sberbank.ditsib.transport.reports.messaging.senders.UpdateRequestStatusFromReportsSender;
import ru.sberbank.ditsib.transport.reports.utils.LogUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UpdateRequestStatusFromReportsSenderImpl implements UpdateRequestStatusFromReportsSender {

    @Qualifier("updateRequestStatusFromReportsOutput")
    private final ObjectProvider<OutputBridge> source;

    private final UpdateRequestStatusMapper mapper;

    @Override
    public void send(UUID requestId, String requestStatus, LocalDateTime dateTime, UUID userId) {
        source.ifAvailable(outputBridge -> outputBridge.send(mapper.toMessage(requestId, requestStatus, dateTime, userId.toString()),
                LogUtils.getStringMap()));
    }
}
