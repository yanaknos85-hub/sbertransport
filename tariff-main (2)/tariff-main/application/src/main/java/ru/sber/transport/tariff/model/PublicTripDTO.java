package ru.sber.transport.tariff.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Данные для расчёта стоимости поездки на общественном транспорте
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Количество билетов на общественном транспорте", description = "Данные о поездке для расчета стоимости")
public class PublicTripDTO {
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Количество билетов/жетонов на метро", minimum = "0", defaultValue = "0")
    private final Integer metroTicketsQuantity = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Количество билетов на трамвай", minimum = "0", defaultValue = "0")
    private final Integer tramTicketsQuantity = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Количество билетов на троллейбус", minimum = "0", defaultValue = "0")
    private final Integer trolleybusTicketsQuantity = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Количество билетов на автобус", minimum = "0", defaultValue = "0")
    private final Integer busTicketsQuantity = 0;
}