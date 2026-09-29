package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @deprecated Используйте ru.sber.transport:approvals-messaging
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class ApproveSharedRideMessage implements Message<UUID> {
    /**
     * Id заявки, которая присоединяется к совместной
     */
    private UUID addRequestId;
    
  //  private UUID actorId;
    
    /**
     * Id заявки-владельца совместной поездки
     */
    private UUID ownerRequestId;
    
    /**
     * approved
     */
    private Boolean approved;
    
    /**
     * approved/declined by
     */
    private UUID approvedByEmployeeId;
    
    private LocalDateTime desiredDate;
    
    private String statusApprove;
    
    /**
     * message (for instance decline reason)
     */
    private String message;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getAddRequestId();
    }
}

