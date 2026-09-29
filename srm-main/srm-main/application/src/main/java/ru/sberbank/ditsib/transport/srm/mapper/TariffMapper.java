package ru.sberbank.ditsib.transport.srm.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TariffMapper {
    
    @Mapping(target = "contractorDeviationParams.maxDiffComputedDistancePercent", source = "maxDiffComputedDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffFactDistancePercent", source = "maxDiffFactDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedCostPercent", source = "maxDiffComputedCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffContractorCostPercent", source = "maxDiffContractorCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedWaitingPercent", source = "maxDiffComputedWaitingPercent")
    
    @Mapping(target = "coopTariffParams.minCancelTimeMin", source = "minCancelTimeMin")
    @Mapping(target = "coopTariffParams.savingsDeviationPct", source = "savingsDeviationPct")
    @Mapping(target = "coopTariffParams.distanceDeviationKm", source = "distanceDeviationKm")
    @Mapping(target = "coopTariffParams.timeDeviationMin", source = "timeDeviationMin")

    @Mapping(target = "timedTariffParams.coefWorkDayMorning", source = "coefWorkDayMorning")
    @Mapping(target = "timedTariffParams.coefWorkDayNoon", source = "coefWorkDayNoon")
    @Mapping(target = "timedTariffParams.coefWorkDayEvening", source = "coefWorkDayEvening")
    @Mapping(target = "timedTariffParams.coefWorkDayNight", source = "coefWorkDayNight")
    @Mapping(target = "timedTariffParams.coefDayOff", source = "coefDayOff")
    TaxiTariff taxiTariffMessageToModel(TaxiTariffMessage message);
    
    @Mapping(target = "engineTariffParams.coefEngine1_6", source = "coefEngine1_6")
    @Mapping(target = "engineTariffParams.coefEngine1_6_to_2_0", source = "coefEngine1_6_to_2_0")
    @Mapping(target = "engineTariffParams.coefEngine2_0_to_2_5", source = "coefEngine2_0_to_2_5")
    
    @Mapping(target = "coopTariffParams.minCancelTimeMin", source = "minCancelTimeMin")
    @Mapping(target = "coopTariffParams.savingsDeviationPct", source = "savingsDeviationPct")
    @Mapping(target = "coopTariffParams.distanceDeviationKm", source = "distanceDeviationKm")
    @Mapping(target = "coopTariffParams.timeDeviationMin", source = "timeDeviationMin")
    
    @Mapping(target = "timedTariffParams.coefWorkDayMorning", source = "coefWorkDayMorning")
    @Mapping(target = "timedTariffParams.coefWorkDayNoon", source = "coefWorkDayNoon")
    @Mapping(target = "timedTariffParams.coefWorkDayEvening", source = "coefWorkDayEvening")
    @Mapping(target = "timedTariffParams.coefWorkDayNight", source = "coefWorkDayNight")
    @Mapping(target = "timedTariffParams.coefDayOff", source = "coefDayOff")
    PersonalTariff personalTariffMessageToModel(PersonalTariffMessage message);
}
