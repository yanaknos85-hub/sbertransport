package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Сущность записи о просроченной по КС заявке */
@Entity
@Table(schema = "reports", name = "request_status_overdue_message")
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
     * Статус заявки (на подключение к корп. каршерингу)
     */
    @Column(name = "carsharing_join_request_status")
    @Enumerated(EnumType.STRING)
    private CarsharingJoinRequestStatus carsharingJoinRequestStatus;
    
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
