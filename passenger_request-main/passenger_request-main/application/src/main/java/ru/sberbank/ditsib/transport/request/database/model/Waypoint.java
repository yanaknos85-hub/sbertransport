package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity describing expected and address linked to each other
 */
@Entity
@Table(schema = "request", name = "waypoint")
@Getter
@Setter
@ToString(exclude = "request")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Waypoint {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Адрес
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id")
    private Address address;
    
    /**
     * Связанная заявка, в которой задана данная точка
     */
    @ManyToOne(/*optional = false, */fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    /**
     * Время ожидания
     */
    @Column(name = "wait_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration waitTime;

    /**
     * Порядок остановок
     */
    @Column(name = "ordering_index", updatable = false, insertable = false)
    private int orderingIndex;
    
    /**
     * Автоматический чекин
     */
    @Column(name = "checkin_automatic", nullable = false)
    @Builder.Default
    private boolean checkinAutomatic = false;
    
    /**
     * Время Автоматический чекин
     */
    @Column(name = "checkin_automatic_time")
    private LocalDateTime checkinAutomaticTime;
    
    /**
     * Ручной чекин
     */
    @Column(name = "checkin_manual", nullable = false)
    @Builder.Default
    private boolean checkinManual = false;
    
    /**
     * Время Ручной чекин
     */
    @Column(name = "checkin_manual_time")
    private LocalDateTime checkinManualTime;
    
    /**
     * Radius
     */
    @Column(name = "radius")
    @Builder.Default
    private Integer radius = 0;
    
    /**
     * Причина отсутствия
     */
    @Column(name = "absence_reason")
    private String absenceReason;
    
    /**
     * Режим чекина
     */
    @Column(name = "checkin_only_manual", nullable = false)
    @Builder.Default
    private boolean checkinOnlyManual = false;
    
    /**
     * Активность
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
    
    @PreRemove
    private void tearDown() {
        this.request = null;
        this.address = null;
    }
}
