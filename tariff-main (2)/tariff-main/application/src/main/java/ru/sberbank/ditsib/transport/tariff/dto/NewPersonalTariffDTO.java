package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * DTO с данными по публикуемому тарифу коменсации личного авто
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу личного авто", description = "Данные по тарифу")
public class NewPersonalTariffDTO extends NewBaseTariffDto {
    /**
     * Стоимость за км
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Schema(description = "Цена за КМ", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "100000")
    private Integer rideCostPerKm;
    
    @Builder.Default
    @Schema(description = "Бесплатных километров пути, включенных в тариф", minimum = "0", maximum = "100")
    @Min(0)
    @Max(100)
    private final Double distanceIncluded = 0d;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость минимальной поездки с включенным расстоянием, коп", minimum = "0",
            maximum = "100000")
    private final Integer minRideDistanceCost = 0;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость за минуту пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "100000")
    private Integer rideCostPerMin;
    
    @Schema(description = "Бесплатных минут пути, включенных в тариф", minimum = "0", maximum = "60")
    @NotNull
    @Builder.Default
    @Min(0)
    @Max(60)
    private final Integer timeIncluded = 0;
    
    
    @Min(0)
    @Max(1000_00)
    @Schema(description = "Стоимость минимальной поездки с включенным временем, коп", minimum = "0", maximum = "100000")
    @Builder.Default
    private final Integer minRideTimeCost = 0;
    
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость минуты ожидания", minimum = "0", maximum = "100000")
    private final Integer waitCostPerMin = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость минуты ожидания в промежуточной точке", minimum = "0", maximum = "100000")
    private final Integer waitCostPerMinIntermediate = 0;
    
    @Positive
    @Max(10)
    @Schema(description = "Сезонный коэффициент", requiredMode = Schema.RequiredMode.REQUIRED, exclusiveMinimum = true, minimum = "0", maximum =
            "10")
    private Double seasonalCoefficient;
    
    /**
     * Дата начала действия сезонного тарифа
     */
    @Builder.Default
    @Schema(description = "Дата начала действия сезонного тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate seasonStart = LocalDate.parse("2000-01-01");
    
    /**
     * Дата завершения действия сезонного тарифа
     */
    @Builder.Default
    @Schema(description = "Дата окончания действия сезонного тарифа, дата окончания должна быть больше даты начала",
            defaultValue = "2000-12-31")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate seasonEnd = LocalDate.parse("2000-12-31");
    
    @Builder.Default
    @Schema(description = "Набор коэффициентов по времени")
    private final TimedTariffParamsDTO timedTariffParams = new TimedTariffParamsDTO();
    
    @Builder.Default
    @Schema(title = "Коэффициенты двигателя личного авто",
            description = "Дополнительные параметры тарифа, используемые для определения коэффициента за объем " +
                          "двигателя личного ТС")
    private final EngineTariffParamsDTO engineTariffParams = new EngineTariffParamsDTO();
    
    @Builder.Default
    @Schema(description = "Параметры тарифа за чертой города")
    private final SuburbTariffParamsDTO suburbTariffParams = new SuburbTariffParamsDTO();
    
    @Builder.Default
    @Schema(description = "Параметры тарифа совместной поездки")
    private final CoopTariffParamsDTO coopTariffParams = new CoopTariffParamsDTO();
    
    @Min(0)
    @Max(10)
    @Schema(description =
                    "Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более N баллов(настраиваемый параметр) \n" +
                    "(каждый балл больше N увеличивает на x%)", defaultValue = "1", maximum = "10")
    @Builder.Default
    private final Double coefTraffic = 1d;
    
    @Min(1)
    @Max(10)
    @Schema(description = "Kоэффициент перевозки ТМЦ", defaultValue = "1", minimum = "1", maximum = "10")
    @Builder.Default
    private final Double coefMaterialAssets = 1d;
    
    @Min(0)
    @Max(100000)
    @Builder.Default
    @Schema(description = "Индекс затрат на страхование, руб./км", defaultValue = "0.0", minimum = "0", maximum = "100000")
    private final double trustIdx = 0.0;
}
