package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель статистических данных
 */

@Entity
@Table(schema = "reports", name = "stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Stats {
    
    /**
     * Идентификатор лимита.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Идентификатор организации.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Год
     */
    @Column(name = "year", nullable = false)
    private Integer year;
    
    /**
     * Месяц
     */
    @Column(name = "month", nullable = false)
    private Integer month;
    
    /**
     * Тип обслуживания
     */
    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportServiceType serviceType;
    
    /**
     * Тип транспорта
     */
    @Column(name = "transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * Выполненные успешно заявки количество
     */
    @Column
    private Long totalExecuted;
    
    /**
     * Отмененные заявки количество
     */
    @Column
    private Long totalCanceled;
    
    /**
     * Не выполенные заявки с промежуточными статусами количество
     */
    @Column
    private Long totalNotExecuted;
    
    /**
     * Сумма по всем заявкам кроме заявок со статусом canceled в рублях
     */
    @Column
    private Long totalSum;
    
    /**
     * Заявки без нарушений контрольных сроков количество
     */
    @Column
    private Long slaWithoutViolation;
    
    /**
     * Заявки с нарушениями контрольных сроков количество
     */
    @Column
    private Long slaWithViolation;
    
    /**
     * Заявки с 4-5 звезд количество
     */
    @Column
    private Long csiStarPositive;
    
    /**
     * Заявки с 1-3 звезд количество
     */
    @Column
    private Long csiStarNegative;
    
    /**
     * Дата создания лимита
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    
}
