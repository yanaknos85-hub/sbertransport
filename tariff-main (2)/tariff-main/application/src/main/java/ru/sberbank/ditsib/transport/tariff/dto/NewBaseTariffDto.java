package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO с информацией о новом тарифе
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Новые данные по тарифу", description = "Данные по тарифу")
public class NewBaseTariffDto {
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
    private final TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    /**
     * Id организации
     */
    @Schema(description = "Идентификатор организации", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID organizationId;
    
    
    /**
     * Price of tariff.
     */
    @Schema(description = "Данные о стоимости тарифа")
    @Singular
    private Map<String, Object> priceDetails;
    
}
