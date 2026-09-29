package ru.sber.transport.request.external.messaging.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * Сообщение для фрод-мониторинга.
 */
public record FraudMonitoringMessage(
        @JsonProperty("requestId")
        UUID id,
        String source,
        List<FraudDataItem> fraudData
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @JsonInclude
    public record FraudDataItem(
            String type,
            String comment,
            String relatedRequestId
    ) {}
}
