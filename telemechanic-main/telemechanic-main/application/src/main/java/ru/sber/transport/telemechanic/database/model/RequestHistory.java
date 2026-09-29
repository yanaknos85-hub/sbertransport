package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * История изменения заявок
 */
@Entity
@Table(name = "request_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class RequestHistory {

    /**
     * Идентификатор записи об истории изменении заявки
     */
    @Id
    @GeneratedValue
    @ToString.Exclude
    private UUID id;

    /**
     * Дата и время изменения заявки
     */
    @NotNull
    private LocalDateTime changeTime;

    /**
     * Новый статус заявки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus status;
    
    /**
     * Старый статус заявки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private RequestStatus oldStatus;

    /**
     * Комментарий к изменению заявки
     */
    private String comment;

    /**
     * Идентификатор записи об инициаторе изменения
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Employee initiator;

    /**
     * Идентификатор записи о заявке
     */
    @ToString.Exclude
    private UUID requestId;
}

