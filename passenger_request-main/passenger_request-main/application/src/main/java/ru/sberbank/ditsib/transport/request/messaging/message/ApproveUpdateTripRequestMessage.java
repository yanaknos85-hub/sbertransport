package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveUpdateTripRequestMessage implements Message<UUID> {
    /**
     * trip update ID..
     */
    private UUID updateId;
    
    /**
     * approved
     */
    private Boolean approved;
    
    /**
     * approved/declined by
     */
    private UUID approvedByEmployeeId;
    
    /**
     * message (for instance decline reason)
     */
    private String message;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getUpdateId();
    }
}

