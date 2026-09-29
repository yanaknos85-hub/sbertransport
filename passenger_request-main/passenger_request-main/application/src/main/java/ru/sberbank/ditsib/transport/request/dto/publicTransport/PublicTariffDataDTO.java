package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO с данными о тарифе
 **/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        title = "DTO с данными о тарифе",
        description = "Данные о тарифе: доступность разных видов городского транспорта и стоимость проезда"
)
@Deprecated
public class PublicTariffDataDTO {
    
    /**
     * Цена билета на метро, коп
     */
    @NotNull @Min(0)
    @Schema(description = "Цена билета на метро, коп")
    private Integer metroTicketCost;
    
    /**
     * Цена билета на трамвай, коп
     */
    @NotNull @Min(0)
    @Schema(description = "Цена билета на трамвай, коп")
    private Integer tramTicketCost;
    
    /**
     * Цена билета на троллейбус, коп
     */
    @NotNull @Min(0)
    @Schema(description = "Цена билета на троллейбус, коп")
    private Integer trolleybusTicketCost;
    
    /**
     * Цена билета на автобус, коп
     */
    @NotNull @Min(0)
    @Schema(description = "Цена билета на автобус, коп")
    private Integer busTicketCost;
    
    /**
     * Доступность метро в регионе
     */
    @NotNull
    @Schema(description = "Доступность метро в регионе")
    private Boolean metroAvailability;
    
    /**
     * Доступность трамвая в регионе
     */
    @NotNull
    @Schema(description = "Доступность трамвая в регионе")
    private Boolean tramAvailability;
    
    /**
     * Доступность троллейбуса в регионе
     */
    @NotNull
    @Schema(description = "Доступность троллейбуса в регионе")
    private Boolean trolleybusAvailability;
    
    /**
     * Доступность автобуса в регионе
     */
    @NotNull
    @Schema(description = "Доступность автобуса в регионе")
    private Boolean busAvailability;
}
