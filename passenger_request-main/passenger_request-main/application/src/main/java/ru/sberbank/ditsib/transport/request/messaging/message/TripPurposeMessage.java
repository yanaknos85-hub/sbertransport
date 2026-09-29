package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TripPurposeMessage implements Message<UUID> {
    
    /**
     * ID..
     */
    private UUID id;
    
    /**
     * Label.
     */
    private String label;

    /**
     * Deleted.
     */
    private boolean deleted;

    /**
     * Organization.
     */
    private UUID organization;

}
