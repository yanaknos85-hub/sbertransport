package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage implements Message<UUID> {
    
    private UUID id;
    
    private String type;
    
    private String value;
    
    @Builder.Default
    private boolean isDefault = false;
    
    private UUID employeeId;
    
    @Builder.Default
    private boolean deleted = false;
    
}
