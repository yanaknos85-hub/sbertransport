package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Сущность записи о просроченной по КС заявке
 */
@Entity
@Table(schema = "oto_cargo", name = "request_status_overdue_message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusOverdue {
    
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;
    
    /**
     * ID заявки
     */
    @Column(name = "request_id")
    private UUID requestId;
    
    /**
     * Статус заявки (на поездку)
     */
    @Column(name = "trip_request_status")
    @Enumerated(EnumType.STRING)
    private TripRequestStatus tripRequestStatus;
    
    /**
     * Единица измерения времени КС
     */
    @Column(name = "deadline_chrono_unit")
    @Enumerated(EnumType.STRING)
    private ChronoUnit deadlineChronoUnit;
    
    /**
     * Значение КС
     */
    @Column(name = "deadline_value", columnDefinition = "int2 (Types#SMALLINT)")
    private Integer deadlineValue;
    
    /**
     * Время, в которое был превышен КС
     */
    @Column(name = "overdue_time")
    private LocalDateTime overdueTime;
}
