package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Object of data about tariffs.
 */

@Value
@Getter
@Builder
@Schema(title = "Краткие данные по тарифу", description = "Короткий вариант данных по тарифу для использования в " +
                                                          "списках")
public class ShortTariffDto {
    
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID id;
    
    @Schema(description = "Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED)
    String humanReadableId;
    
    @Deprecated
    @Schema(description = "Регион действия. Устаревшее. Используйте `regionId`. Будет удалено через 2 релиза " +
                          "(2021-06-10)",
            deprecated = true)
    String region;
    
    @Schema(description = "Регион действия", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID regionId;
    
    @Schema(description = "Вид транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    TransportTypeEnum transportType;
    
    @Builder.Default
    @Schema(description = "Вид транспортной услуги", requiredMode = Schema.RequiredMode.REQUIRED)
    TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    
    @Builder.Default
    @Schema(description = "Статус (false - удален)", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean active = true;
    
    @Schema(description = "Организация владелец тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID organizationId;
    
    @Schema(description = "Контракт")
    UUID contractId;
    
    @Schema(description = "Номер контракта")
    String contractNumber;
    
    @Schema(description = "Контрагент")
    UUID contractorId;
    
    @Schema(description = "Наименование контрагентаy")
    String contractorName;
    
    @Schema(description = "Человекочитаемый ID подразделения")
    String departmentHumanReadableId;
    
    @Builder.Default
    @Schema(description = "Признак ночного тарифа")
    private Boolean isNightTariff = false;
    
    @Schema(description = "Тип договора")
    ContractType contractType;
}
