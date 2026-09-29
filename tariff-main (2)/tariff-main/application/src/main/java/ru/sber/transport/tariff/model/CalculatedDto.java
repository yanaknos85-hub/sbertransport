package ru.sber.transport.tariff.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.dto.tariff.TransportTypeDto;

import java.util.Map;
import java.util.UUID;

/**
 * Объект с рассчитанными данными.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "Рассчитанные данные", description = "Рассчитанные данные о стоимости проезда по маршруту")
public class CalculatedDto {
    
    /**
     * Коэф. объекма двигателя.
     */
    public static final String ENGINE_VOLUME_COEFFICIENT = "engineVolumeCoef";
    
    /**
     * Нужен коммент
     */
    public static final String EXPRESS_COST = "expressCost";
    
    /**
     * Нужен коммент
     */
    public static final String LOADER_COST = "loaderCost";
    
    /**
     * Нужен коммент
     */
    public static final String LOADER_TARIFF = "loaderTariff";
    
    /**
     * Базовая стоимость.
     */
    public static final String BASE_COST = "baseCost";
    
    /**
     * Базовый тариф.
     */
    public static final String BASE_TARIFF = "baseTariff";
    
    /**
     * Множитель копеек.
     */
    public static final int CENT = 100;
    
    /**
     * ID of tariff.
     */
    @NotNull
    @Schema(description = "Идентификатор использованого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * ID of outcome tariff.
     */
    @NotNull
    @Schema(description = "Идентификатор использованого расходного тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID outcomeTariffId;
    
    /**
     * Type of tariff.
     */
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeDto transportType;
    
    /**
     * Cost.
     */
    @NotNull
    @PositiveOrZero
    @Schema(description = "Стоимость поездки в копейках", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private long cost;
    
    /**
     * Cost.
     */
    @NotNull
    @PositiveOrZero
    @Schema(description = "Стоимость поездки по расходному тарифу в копейках", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private long outcomeCost;
    
    /**
     * Class of taxi.
     */
    @Schema(description = "Дополнительные данные по ценам")
    @Singular
    private Map<String, Object> priceDetails;
    
    /**
     * Class of taxi.
     */
    @Schema(description = "Класс такси. Только для такси")
    private TaxiClass taxiClass;

    /**
     * Класс группового трансфера
     */
    @Schema(description = "Класс группового трансфера")
    private String groupTransferClass;
    
    /**
     * For cargo Import
     */
    @Schema(description = "активный / не активный тариф")
    private Boolean active;
    
    
    /**
     * Доступность лимита.
     */
    @Schema(description = "Доступность лимита")
    private boolean limitAvailable;
}
