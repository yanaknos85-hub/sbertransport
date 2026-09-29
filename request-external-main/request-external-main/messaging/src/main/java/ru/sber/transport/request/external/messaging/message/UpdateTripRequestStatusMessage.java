package ru.sber.transport.request.external.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequestStatusMessage implements Message<UUID> {
    
    private UUID id;
    
    private String status;
    
    private LocalDateTime dateTime;
    
    private String userId;
}
