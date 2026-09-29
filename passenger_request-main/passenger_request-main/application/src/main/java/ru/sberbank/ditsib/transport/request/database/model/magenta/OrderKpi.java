package ru.sberbank.ditsib.transport.request.database.model.magenta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO с KPI заказа из совместной поездки
 */
@Entity
@Table(schema = "request", name = "order_kpi")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "kpi")
@Builder
@Schema(title = "KPI заказа из совместной поездки", description = "Рассчитанные параметры заказа на поездку")
public class OrderKpi {
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Родительский объект
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id")
    private SharedRideKPI kpi;
    
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    @Column(name = "order_id", columnDefinition = "int8")
    private Integer orderId;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Column(name = "cost_share_part")
    private Double costSharePart;
    
    /**
     * рассчитанное время поездки для текущего заказа
     */
    @Column(name = "ride_time_min")
    private Integer rideTimeMin;
    
    /**
     * экономия в рублях для текущего заказа
     */
    @Column(name = "savings")
    private double savings;
    
    /**
     * экономия в процентах для текущего заказа
     */
    @Column(name = "savings_pct")
    private double savingsPct;
    
    /**
     * Рассчитанный километраж поездки для текущего заказа
     */
    @Column(name = "order_distance_km")
    private Integer orderDistanceKm;
}
