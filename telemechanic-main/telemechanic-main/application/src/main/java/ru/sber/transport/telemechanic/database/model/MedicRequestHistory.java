package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * История изменения медицинских заявок
 */
@Entity
@Table(name = "medic_request_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class MedicRequestHistory {
    
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
     * Статус заявки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TelemedicineStatus status;
    
    /**
     * Статус заявки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private TelemedicineStatus oldStatus;
    
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
    private UUID medicRequestId;
}
