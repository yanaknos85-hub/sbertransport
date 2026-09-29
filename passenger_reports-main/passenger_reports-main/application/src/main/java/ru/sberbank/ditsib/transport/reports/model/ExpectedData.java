package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
