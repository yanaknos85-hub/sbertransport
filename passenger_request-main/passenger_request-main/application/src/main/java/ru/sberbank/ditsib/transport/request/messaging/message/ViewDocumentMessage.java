package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message with view document
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewDocumentMessage implements Message<UUID> {

    /**
     * document id
     */
    private UUID documentId;
    
    /**
     * user that viewed request document
     */
    private UUID employeeId;
    
    /**
     * view date time
     */
    private LocalDateTime dateTime;
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return getDocumentId();
    }
}
