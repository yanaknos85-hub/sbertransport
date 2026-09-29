package ru.sber.transport.tariff.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Object of data about tariffs.
 */
@Builder(toBuilder = true)
@Schema(title = "Данные по тарифу", description = "Данные по тарифу")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseTariffDataDto {
    /**
     * ID.
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * ID (human readable).
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * Name of region.
     */
    @Deprecated
    @Schema(description = "Территория действия тарифа. Устаревшее. Используйте `regionId`. Будет удалено через 2 " +
                          "релиза (2021-06-10)", deprecated = true)
    private String region;
    
    @NotNull
    @Schema(description = "Территория действия тарифа",requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<UUID> regionId;
    
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    @Builder.Default
    @Schema(description = "Вид транспортной услуги")
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    /**
     * Id организации
     */
    @NotNull
    @Schema(description = "Идентификатор организации", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID organizationId;
    
    /**
     * Price of tariff.
     */
    @Schema(description = "Данные о стоимости тарифа")
    @Singular
    private Map<String, Object> priceDetails;
    
    @NotNull
    @Schema(description = "Идентификатор контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractId;
    
    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;
    
    @Schema(description = "Рабочая группа")
    private String workgroup;
    
    /**
     * Флаг активности
     */
    @Schema(description = "Флаг активности")
    private boolean active;
}

