package ru.sberbank.transport.oto.cargo.messaging.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestStatusOverdueMessage(UUID id, UUID requestId, String tripRequestStatus,
                                          String carsharingJoinRequestStatus, String deadlineChronoUni,
                                          int deadlineValue, LocalDateTime overdueTime) implements Message<UUID> {

    @JsonIgnore
    public UUID getId() {
        return this.id;
    }
}
