package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * DTO с данными по публикуемому тарифу каршеринга
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу каршеринга", description = "Данные по тарифу")
public class NewCarSharingTariffDTO extends NewBaseTariffDto {
    
    @NotNull
    @Schema(description = "Идентификатор контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;
    
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Schema(description = "Цена за КМ", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerKm;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость за минуту пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerMin;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость времени ожидания при бронировании и аренде ТС, коп.", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer waitCostPerMin;
    
    @Builder.Default
    @Schema(description = "Набор коэффициентов по времени")
    private final TimedTariffParamsDTO timedTariffParams = new TimedTariffParamsDTO();
    
    @Min(0)
    @Max(10)
    @Builder.Default
    @Schema(description = "Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов (каждый балл " +
                          "больше 7 увеличивает на x%)", defaultValue = "1", minimum = "0", maximum = "10")
    private final Double coefTraffic = 1d;
    
    @Min(1)
    @Max(10)
    @Builder.Default
    @Schema(description = "Коэффициент доплаты за детское кресло", defaultValue = "1", minimum = "1", maximum = "10")
    private final Double coefChildSeat = 1d;
    
    @Min(1)
    @Max(10)
    @Builder.Default
    @Schema(description = "Коэффициент доплаты за перевозку животного", defaultValue = "1", minimum = "1",
            maximum = "10")
    private final Double coefPetTransport = 1d;
    
    @Positive
    @Max(10)
    @Builder.Default
    @Schema(description = "Коэффициент на полное покрытие ответственности КАСКО", defaultValue = "1", minimum = "1",
            maximum = "10")
    private final Double coefCasko = 1d;
}
