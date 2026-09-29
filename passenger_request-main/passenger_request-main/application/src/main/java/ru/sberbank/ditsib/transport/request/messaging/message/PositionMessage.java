package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Position message.
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionMessage implements Message<UUID> {
    
    /**
     * ID of position.
     */
    private UUID id;
    
    /**
     * ID of parent organization.
     */
    private UUID organizationId;
    
    /**
     * name of position.
     */
    private String positionName;
    
    /**
     * Available classes of taxi
     */
    @Builder.Default
    private Set<String> availableClasses = new HashSet<>();
    
    /**
     * Flag stating that position doen§t need approval fro requests
     */
    private boolean selfApproved;
    
    /**
     * Flag of position deleted.
     */
    @Builder.Default
    private boolean deleted = false;
}
