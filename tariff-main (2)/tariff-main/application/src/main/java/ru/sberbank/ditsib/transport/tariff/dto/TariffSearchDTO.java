package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;

import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Schema(title = "Поиск тарифов", description = "Фильтры для поиска тарифов")
public class TariffSearchDTO {
    @Schema(description = "Тип транспорта")
    private final TransportTypeEnum transportType;
    
    @Schema(description = "Вид транспортной услуги")
    private final TransportServiceType serviceType;
    
    @Schema(description = "Идентификатор организации")
    private final UUID organizationId;
    
    @Schema(description = "Идентификатор контракта")
    private final UUID contractId;
    
    @Schema(description = "Номер контракта")
    private final String contractNumber;
    
    @Schema(description = "Идентификатор контрагента")
    private final UUID contractorId;
    
    @Schema(description = "Человеко читаемый id")
    private final String humanReadableId;
    
    @Schema(description = "Регион действия тарифа")
    private final UUID regionId;
    
    @Schema(description = "Класс транспорта")
    private final TransportClass transportClass;
    
    @Schema(description = "Флаг активности")
    private final Boolean active;
    
    @Schema(description = "Только ночные тарифы")
    private final Boolean isNightTariff;
    
    @Schema(description = "Тип договора")
    private final ContractType contractType;
}
