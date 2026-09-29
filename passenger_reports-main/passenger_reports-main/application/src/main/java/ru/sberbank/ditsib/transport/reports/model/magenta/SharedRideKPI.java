package ru.sberbank.ditsib.transport.reports.model.magenta;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.util.*;

/**
 * Entity расчетного KPI совместной поездки
 */
@Entity
@Table(schema = "reports", name = "kpi")
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
public class SharedRideKPI {
    
    /**
     * Идентификатор
     */
    @Id
    private UUID id;
    
    /**
     * Полная стоимость
     */
    @Column(name = "total_cost")
    private Double totalCost;
    
    /**
     * Полное расстояние
     */
    @Column(name = "total_distance_km")
    private Double totalDistanceKm;
    
    /**
     * Рассчитанное время поездки
     */
    @Column(name = "total_time_min")
    private Integer totalTimeMin;
    
    /**
     * KPI отдельных заказов
     */
    @OneToMany(orphanRemoval = true, mappedBy = "kpiId")
    @Builder.Default
    private final Set<OrderKpi> ordersKpi = new HashSet<>();
    
}
