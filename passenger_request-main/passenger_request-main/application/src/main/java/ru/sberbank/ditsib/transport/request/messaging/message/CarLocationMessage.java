package ru.sberbank.ditsib.transport.request.messaging.message;

import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CarLocationMessage(
        UUID id,
        Map<UUID, ContractorInfo> contractorRequests
) implements Message<UUID> {

    public record ContractorInfo(
            String url,
            String login,
            String password,
            List<String> orderPartnerIds
    ) {}

    @Override
    public UUID getId() {
        return id;
    }
}
