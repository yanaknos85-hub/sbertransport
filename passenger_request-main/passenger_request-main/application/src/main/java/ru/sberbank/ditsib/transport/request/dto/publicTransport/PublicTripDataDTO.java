package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO с данными о поездке
 **/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        title = "DTO с данными о поездке",
        description = "Данные о поездке: количество билетов, которые необходимо купить для совершения поездки"
)
@Deprecated
public class PublicTripDataDTO {
    
    /**
     * Количество билетов/жетонов на метро
     */
    @Min(0)
    @Schema(description = "Количество билетов/жетонов на метро")
    private Integer metroTicketsQuantity;
    
    /**
     * Количество билетов на трамвай
     */
    @Min(0)
    @Schema(description = "Количество билетов на трамвай")
    private Integer tramTicketsQuantity;
    
    /**
     * Количество билетов на троллейбус
     */
    @Min(0)
    @Schema(description = "Количество билетов на троллейбус")
    private Integer trolleybusTicketsQuantity;
    
    /**
     * Количество билетов на автобус
     */
    @Min(0)
    @Schema(description = "Количество билетов на автобус")
    private Integer busTicketsQuantity;
}
