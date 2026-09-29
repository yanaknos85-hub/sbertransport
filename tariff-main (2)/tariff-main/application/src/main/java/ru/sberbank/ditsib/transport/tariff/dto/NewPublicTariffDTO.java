package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO с данными по публикуемому тарифу общественного транспрорта
 */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу на компенсацию общественного транспорта", description = "Данные по тарифу")
public class NewPublicTariffDTO extends NewBaseTariffDto {
    
    @NotNull
    @Min(0)
    @Schema(description = "Цена билета на метро, коп", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer metroTicketCost;
    
    @NotNull
    @Min(0)
    @Schema(description = "Цена билета на трамвай, коп", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer tramTicketCost;
    
    @NotNull
    @Min(0)
    @Schema(description = "Цена билета на троллейбус, коп", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer trolleybusTicketCost;
    
    @NotNull
    @Min(0)
    @Schema(description = "Цена билета на автобус, коп", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer busTicketCost;
    
    //@NotNull
    //@Min(0)
    @Builder.Default
    @Schema(description = "Цена билета на электричку, коп", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cityLocalTrainCost = 0;
    
    @NotNull
    @Schema(description = "Доступность метро в регионе", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean metroAvailability;
    
    @NotNull
    @Schema(description = "Доступность трамвая в регионе", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tramAvailability;
    
    @NotNull
    @Schema(description = "Доступность троллейбуса в регионе", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean trolleybusAvailability;
    
    @NotNull
    @Schema(description = "Доступность автобуса в регионе", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean busAvailability;
    
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность электрички в регионе", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean cityLocalTrainAvailability = false;
    
    //--------------------------
    
    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость проездного(мес) на метро, коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardMetroCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность проездного на метро", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardMetroAvailability = false;
    
    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость проездного(мес) на трамвай, коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardTramCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность проездного на трамвай", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardTramAvailability = false;
    
    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость проездного(мес) на троллейбус, коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardTrolleybusCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность проездного на троллейбус", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardTrolleybusAvailability = false;

    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость проездного(мес) на автобус, коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardBusCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность проездного на автобус", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardBusAvailability = false;

    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость проездного(мес) на электричку, коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardLocalTrainCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность проездного на электричку", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardLocalTrainAvailability = false;

    //@NotNull
    @Min(0)
    @Max(100000_00)
    @Builder.Default
    @Schema(description = "Стоимость единого проездного(мес), коп", minimum = "0", maximum = "100000_00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer travelCardAllCityTransportCost = 0;
    //@NotNull
    @Builder.Default
    @Schema(description = "Доступность единого проездного", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean travelCardAllCityTransportAvailability = false;

}
