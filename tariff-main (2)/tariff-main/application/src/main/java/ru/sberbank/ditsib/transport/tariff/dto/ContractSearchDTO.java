package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Schema(title = "Поиск контрактов", description = "Фильтры для поиска контрактов")
public class ContractSearchDTO {
    
    @Schema(description = "Тип транспорта")
    private final TransportTypeEnum transportType;
    
    @Schema(description = "Вид транспортной услуги")
    private final TransportServiceType serviceType;
    
    @Schema(description = "Идентификатор организации")
    private final UUID organizationId;
    
    @Schema(description = "Идентификатор контрагента")
    private final UUID contractorId;
    
    @Schema(description = "Регион действия контракта")
    private final UUID region;
    
    @Schema(description = "Регионы действия контракта")
    private final List<UUID> regionIds;
    
    @Schema(description = "Номер контракта")
    private final String contractNumber;
    
    @Schema(description = "Номер договора УВХД")
    private final String uvhd;
    
    @Schema(description = " Начало диапазона (завершение контракта не меньше этой даты)")
    private final LocalDate startDate;
    
    @Schema(description = " Конец диапазона (начало контракта не больше этой даты)")
    private final LocalDate endDate;
    
    @Schema(description = "Флаг активности")
    private final Boolean active;
    
    @Schema(description = "Тип договора")
    private final ContractType contractType;
}
