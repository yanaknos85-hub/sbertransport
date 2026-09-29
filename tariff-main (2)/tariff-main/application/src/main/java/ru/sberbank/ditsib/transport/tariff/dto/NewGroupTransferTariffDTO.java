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
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * DTO с данными по публикуемому тарифу групповому трансферу
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу групповому трансферу", description = "Данные по тарифу")
public class NewGroupTransferTariffDTO extends NewBaseTariffDto {
    
    @Schema(description = "Идентификатор тарифа в системе контрагента( Для Gett обязательное поле)")
    private String contractorTariffId;
    
    @NotNull
    @Schema(description = "Идентификатор контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;
    
    @NotNull
    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;
    
    @Schema(description = "Данные о подразделении")
    private TariffDepartmentDTO department;
    
    @NotNull
    @Schema(description = "Класс группового трансфера", requiredMode = Schema.RequiredMode.REQUIRED)
    private GroupTransferClass groupTransferClass;
    
    @Schema(description = "Вип тариф", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private boolean vip;
    
    @Schema(description = "Дата начала действия тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate tariffStartDate;
    
    @Schema(description = "Дата окончания действия тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate tariffEndDate;
    
    @Min(0)
    @NotNull
    @Schema(description = "Цена за км пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerKm;
    
    @Min(0)
    @NotNull
    @Schema(description = "Стоимость за минуту пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerMin;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Минимальное время поездки в минутах", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer minMin = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Минимальная протяженность маршрута в км", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer minKm = 0;
    
    @Schema(description = "Стоимость минимальной поездки, коп", requiredMode = Schema.RequiredMode.REQUIRED,
            defaultValue = "0")
    @Builder.Default
    private Integer minRideCost = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Бесплатное время ожидания в минутах ", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer freeWaitingTime = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Цена за км в городе, коп", defaultValue = "0", minimum = "0")
    private Integer costPerKmCity = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Цена за минуту в городе, коп", defaultValue = "0", minimum = "0")
    private Integer costPerMinCity = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Цена за км  за чертой города, коп", defaultValue = "0", minimum = "0")
    private Integer costPerKmSuburb = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Цена за минуту за чертой города, коп", defaultValue = "0", minimum = "0")
    private Integer costPerMinSuburb = 0;
    
    @Min(0)
    @Schema(description = "Стоимость минуты ожидания, коп", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer waitCostPerMin;
    
    @Schema(description = "Детское кресло", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private boolean childSeat;
    
    @Schema(description = "Негабаритный багаж", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private boolean bugOversized;
    
    @Schema(description = "Животные", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private boolean animal;
    
    @PositiveOrZero
    @Builder.Default
    @Schema(description = "Минимальное время для формирования заказа в минутах.", defaultValue = "1440")
    private Integer minCreateTime = 1440;
    
    @PositiveOrZero
    @Builder.Default
    @Schema(description = "Минимальное время отмены в минутах.", defaultValue = "360")
    private Integer minCancelTime = 360;
    
    @PositiveOrZero
    @Builder.Default
    @Schema(description = "Триггерное время в минутах. Заявка будет отправлена на исполнение в момент Tжелаемое - Ttrigger", defaultValue = "120")
    private Integer triggerTime = 120;
    
    @Schema(description = "Список автомобилей (диспетчерская)")
    private Set<UUID> transportIds;
    
    /**
     * Новое поле на замену, так как на бэке не предусмотрена логика по нескольким регионам ru.sberbank.ditsib.transport.tariff.dto.NewBaseTariffDto#regionId
     */
    @NotEmpty
    @Schema(description = "Список регионов", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<UUID> regionIds;
    
}
