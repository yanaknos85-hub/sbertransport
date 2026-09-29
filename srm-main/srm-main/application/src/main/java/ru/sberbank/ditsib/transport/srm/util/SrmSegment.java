package ru.sberbank.ditsib.transport.srm.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.UUID;

/**
 * DTO с KPI заказа из совместной поездки
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Schema(title = "KPI заказа из совместной поездки", description = "Рассчитанные параметры заказа на поездку")
public class SrmSegment {
    
    /**
     * Уникальный ключ записи
     */
    private UUID id;
    
    /**
     * Точка начала сегмента.
     */
    SrmWaypoint startWaypoint;
    
    /**
     * Точка конца сегмента.
     */
    SrmWaypoint endWaypoint;
    
    /**
     * Рассчитанный километраж сегмента для текущего заказа в км
     */
    private Double segmentDistance;
    
    /**
     * Oбщий вес сегмента по всем заявкам в кг.
     */
    private Double segmentWeight;
    
    /**
     * Рассчитанная цена сегмента по тарифу в копейках
     */
    private Long segmentPrice;
    
    /**
     * Рассчитанная цена сегмента по тарифу в копейках
     */
    private Integer requestKpiNumber;
}
