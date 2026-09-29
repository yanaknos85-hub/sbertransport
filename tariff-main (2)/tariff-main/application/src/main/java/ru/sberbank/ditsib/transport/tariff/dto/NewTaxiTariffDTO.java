package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO с данными по публикуемому тарифу такси
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу такси", description = "Данные по тарифу")
public class NewTaxiTariffDTO extends NewBaseTariffDto {
    
    @Schema(description = "Идентификатор тарифа в системе контрагента( Для Gett обязательное поле)")
    private String contractorTariffId;
    
    @NotNull
    @Schema(description = "Идентификатор контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;
    
    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;
    
    @Schema(description = "Данные о подразделении")
    private TariffDepartmentDTO department;
    
    @NotNull
    @Schema(description = "Класс такси", requiredMode = Schema.RequiredMode.REQUIRED)
    private TaxiClass taxiClass;
    
    /**
     * Стоимость за км
     */
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Цена за км пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "100000")
    private Integer rideCostPerKm;
    
    @Builder.Default
    @Schema(description = "Бесплатных километров пути, включенных в тариф", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "0",
            minimum = "0", maximum = "100")
    @Min(0)
    @Max(100)
    private final Double distanceIncluded = 0d;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Стоимость минимальной поездки с включенным расстоянием, коп", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "0")
    private final Integer minRideDistanceCost = 0;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость за минуту пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "100000")
    private Integer rideCostPerMin;
    
    @Schema(description = "Бесплатных минут пути, включенных в тариф", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "0", minimum = "0")
    @NotNull
    @Builder.Default
    @Min(0)
    private final Integer timeIncluded = 0;
    
    @Schema(description = "Стоимость минимальной поездки с включенным временем, коп", requiredMode = Schema.RequiredMode.REQUIRED,
            defaultValue = "0")
    @Builder.Default
    private final Integer minRideTimeCost = 0;
    
    //Стоимость за минуту ожидания в начальной точке
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Schema(description = "Стоимость минуты ожидания", requiredMode = Schema.RequiredMode.REQUIRED, maximum = "100000")
    private Integer waitCostPerMin;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость за минуту ожидания в промежуточной точке, коп.", minimum = "0")
    private final Integer waitCostPerMinIntermediate = 0;
    
    @Min(0)
    @Max(60)
    @Builder.Default
    @Schema(description = "Бесплатных минут ожидания, включенных в тариф", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private final Integer freeWaitingTime = 0;
    
    @Builder.Default
    @Schema(description = "Набор коэффициентов по времени")
    private final TimedTariffParamsDTO timedTariffParams = new TimedTariffParamsDTO();
    
    @Builder.Default
    @Schema(description = "Параметры тарифа за чертой города")
    private final SuburbTariffParamsDTO suburbTariffParams = new SuburbTariffParamsDTO();
    
    @Builder.Default
    @Schema(description = "Параметры поиска совместных поездок")
    private final CoopTariffParamsDTO coopTariffParams = new CoopTariffParamsDTO();
    
    @Builder.Default
    @Schema(description = "Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента")
    private final ContractorDeviationsTariffDTO contractorDeviationParams = new ContractorDeviationsTariffDTO();
    
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов (каждый балл " +
                          "больше 7 увеличивает на x%)", minimum = "0")
    private final Double coefTraffic = 0d;
    
    @Min(1)
    @Builder.Default
    @Schema(description = "Коэффициент доплаты за детское кресло", minimum = "1")
    private final Double coefChildSeat = 1d;
    
    @Min(1)
    @Builder.Default
    @Schema(description = "Коэффициент доплаты за перевозку животного", minimum = "1")
    private final Double coefPetTransport = 1d;
    
    @Min(1)
    @Builder.Default
    @Schema(description = "Коэффициент доплаты за лыжи/сноуборд/велосипед", minimum = "1")
    private final Double coefBicycle = 1d;
    
    @Positive
    @Builder.Default
    @Schema(description = "Коэффициент организации", defaultValue = "1")
    private final Double coefOrg = 1d;
    
    /**
     * Триггерное время
     */
    @PositiveOrZero
    @Builder.Default
    @Schema(description = "Триггерное время в минутах. Заявка будет отправлена на исполнение в момент Tжелаемое - Ttrigger", defaultValue = "60")
    private final Integer triggerTime = 60;
    
    @Builder.Default
    @Schema(description = "Признак ночного тарифа")
    private Boolean isNightTariff = false;
}
