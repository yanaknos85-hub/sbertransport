package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TripRatingMessage implements Message<UUID> {
    
    private UUID requestId;
    
    /**
     * Collection of request advantages selected by user
     */
    private final Set<String> advantages = new HashSet<>();
    
    /**
     * Collection of request drawbacks selected by user
     */
    private final Set<String> drawbacks = new HashSet<>();
    
    /**
     * Rating
     */
    private int rating;
    
    /**
     * Comment
     */
    private String ratingComment;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getRequestId();
    }
}