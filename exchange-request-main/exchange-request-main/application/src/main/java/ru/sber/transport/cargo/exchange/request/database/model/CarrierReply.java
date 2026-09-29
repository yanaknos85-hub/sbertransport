package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "exchange_request", name = "request_carrier_reply")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierReply {
    /**
     * Уникальный идентификатор отклика (UUID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Заявка, к которой относится отклик
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /**
     * Детали отклика
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private CarrierReplyDto reply;

    /**
     * Организация перевозчика
     */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Дата и время создания записи
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Признак выбранного отклика
     */
    private boolean selected;
}
