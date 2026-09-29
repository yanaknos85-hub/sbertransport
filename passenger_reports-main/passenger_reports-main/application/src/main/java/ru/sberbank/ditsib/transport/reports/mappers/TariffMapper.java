package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TariffMapper {
    
    @Mapping(target = "contractorDeviationParams.maxDiffComputedDistancePercent", source = "maxDiffComputedDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffFactDistancePercent", source = "maxDiffFactDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedCostPercent", source = "maxDiffComputedCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffContractorCostPercent", source = "maxDiffContractorCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedWaitingPercent", source = "maxDiffComputedWaitingPercent")
    TaxiTariff taxiTariffMessageToModel(TaxiTariffMessage message);
}
