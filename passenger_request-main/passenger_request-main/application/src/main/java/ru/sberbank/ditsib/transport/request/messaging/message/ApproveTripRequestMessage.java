package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.List;
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
public class ApproveTripRequestMessage implements Message<UUID> {
    
    /**
     * actor ID..
     */
    private UUID actionId;

    /**
     * approved. Значение NULL в случае, если сообщение означает создание объекта согласования.
     */
    private Boolean approved;

    /**
     * approved/declined by
     */
    private UUID actorEmployeeId;
    
    /**
     * сообщение (в случае отклонения заявки)
     */
    private String message;
    
    /**
     * Если approved NULL, то отправляется список с ID сотрудников, которые могут согласовать заявку
     */
    private List<UUID> approverIds;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getActionId();
    }
}
