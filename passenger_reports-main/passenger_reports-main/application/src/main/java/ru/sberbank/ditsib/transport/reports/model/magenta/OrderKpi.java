package ru.sberbank.ditsib.transport.reports.model.magenta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * DTO с KPI заказа из совместной поездки
 */
@Entity
@Table(schema = "reports", name = "order_kpi")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
@Schema(title = "KPI заказа из совместной поездки", description = "Рассчитанные параметры заказа на поездку")
public class OrderKpi {
    /**
     * Идентификатор
     */
    @Id
    private UUID id;
    
    /**
     * Родительский объект
     */
    private UUID kpiId;
    
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    @Column(name = "request_id")
    private UUID requestId;
    
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
    private Double savings;
    
    /**
     * экономия в процентах для текущего заказа
     */
    @Column(name = "savings_pct")
    private Double savingsPct;
    
    /**
     * Рассчитанный километраж поездки для текущего заказа
     */
    @Column(name = "order_distance_km")
    private Integer orderDistanceKm;
}
