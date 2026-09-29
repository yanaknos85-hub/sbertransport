package ru.sberbank.ditsib.transport.request.database.model.magenta;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity расчетного KPI совместной поездки
 */
@Entity
@Table(schema = "request", name = "kpi")
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedRideKPI {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue
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
    @OneToMany(orphanRemoval = true, mappedBy = "kpi", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private final List<OrderKpi> ordersKpi = new ArrayList<>();
    
}
