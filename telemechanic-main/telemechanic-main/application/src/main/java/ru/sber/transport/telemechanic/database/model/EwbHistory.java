package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * История изменения ЭПЛ
 */
@Entity
@Table(name = "ewb_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class EwbHistory {
    
    /**
     * Идентификатор записи об истории изменения ЭПЛ
     */
    @Id
    @GeneratedValue
    @ToString.Exclude
    private UUID id;
    
    /**
     * Дата и время изменения ЭПЛ
     */
    @NotNull
    private LocalDateTime changeTime;
    
    /**
     * Новый статус ЭПЛ
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EwbStatus status;
    
    /**
     * Старый статус ЭПЛ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private EwbStatus oldStatus;
    
    /**
     * Комментарий к изменению ЭПЛ
     */
    private String comment;
    
    /**
     * Идентификатор записи об инициаторе изменения
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Employee initiator;
    
    /**
     * Идентификатор записи ЭПЛ
     */
    @ToString.Exclude
    private UUID ewbId;
}
