package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Коэффициенты за объем двигателя
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EngineTariffParams {
    
    /**
     * Граница двигателей 1.6.
     */
    public static final int BORDER_1_6 = 1600;
    
    /**
     * Граница двигателей 2.0.
     */
    public static final int BORDER_2_0 = 2000;
    
    /**
     * Граница двигателей 2.5.
     */
    public static final int BORDER_2_5 = 2500;
    
    /**
     * К объема двигателя для ТС до 1,6 л. ( только для бензиновых двигателей).
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_engine_1_6")
    private double coefEngine1_6 = 1d;
    
    /**
     * К объема двигателя для ТС от 1,6 до 2,0 л (только для бензиновых двигателей).
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_engine_1_6_to_2_0")
    private double coefEngine1_6_to_2_0 = 1d;
    
    /**
     * К объема двигателя для ТС от 2,0 до 2,5 л (только для бензиновых двигателей).
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_engine_2_0_to_2_5")
    private double coefEngine2_0_to_2_5 = 1d;
}
