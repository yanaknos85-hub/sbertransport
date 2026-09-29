package ru.sberbank.ditsib.transport.reports.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "inbox_message")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class InboxMessage {
    @Id
    @Column
    private UUID messageId;
    
    @Column
    private UUID entityId;
    
    @Column(length = 150)
    private String className;
    
    /**
     * Тип RequestMessage используется как временное решение для закрытия задачи TRANSPORT-31967. Требуется изменить тип данного поля с целью хранить
     * в БД сообщения разных форматов.
     */
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private RequestMessage payload;
    
    @Column
    private LocalDateTime receivedAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private String status;
    
    @Column
    private String errorReason;
    
    public InboxMessage(UUID messageId, UUID entityId, RequestMessage payload) {
        this.messageId = messageId;
        this.entityId = entityId;
        this.payload = payload;
        className = payload.getClass().getName();
        receivedAt = LocalDateTime.now();
        status = InboxMessageStatusEnum.NEW.name();
    }
}
