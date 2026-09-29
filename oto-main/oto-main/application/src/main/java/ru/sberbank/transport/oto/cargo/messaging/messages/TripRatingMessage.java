
package ru.sberbank.transport.oto.cargo.messaging.messages;

import ru.sber.transport.messaging.Message;

import java.util.Set;
import java.util.UUID;

public record TripRatingMessage(UUID requestId, Set<String> advantages, Set<String> drawbacks, int rating,
                                String ratingComment) implements Message<UUID> {
    @Override
    public UUID getId() {
        return requestId;
    }
}
