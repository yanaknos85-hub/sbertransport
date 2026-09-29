package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Duration;

/**
 * Расчтетные данные поездки
 */
@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpectedData {
    
    /**
     * Стоимость
     */
    @Column(name = "expected_cost")
    private Double cost;
    
    /**
     * Стоимость
     */
    @Column(name = "outcome_expected_cost")
    private Double outcomeCost;
    
    @Column(name = "bonus_cost")
    private Long bonusCost;
    
    /**
     * Расстояние
     */
    @Column(name = "expected_distance")
    private Double distance;
    
    /**
     * Время
     */
    @Column(name = "expected_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration time;
}
