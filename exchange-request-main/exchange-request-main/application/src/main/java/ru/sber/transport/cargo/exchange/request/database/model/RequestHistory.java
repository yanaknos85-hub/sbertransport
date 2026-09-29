package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для таблицы exchange_request.request_history.
 * Хранит историю изменений статусов заявки с указанием инициатора действия.
 */
@Entity
@Table(name = "request_history", schema = "exchange_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(name = "comment")
    private String comment;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_request_history_request"))
    private Request request;

    @Column(name = "initiator_id", nullable = false)
    private UUID initiatorId;

    /**
     * Роль инициатора изменения: CARRIER (перевозчик) или SHIPPER (грузовладелец).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_role", nullable = false, length = 128)
    private Role initiatorRole;

}