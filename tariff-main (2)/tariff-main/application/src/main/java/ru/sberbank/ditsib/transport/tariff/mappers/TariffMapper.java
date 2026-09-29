package ru.sberbank.ditsib.transport.tariff.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff.messaging.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Маппер тарифов.
 */
@Mapper(uses = ContractMapper.class)
public interface TariffMapper {
    
    /**
     * Маппинг тарифа такси в сообщение.
     *
     * @param tariff тариф.
     * @param deleted флаг удаления.
     *
     * @return сообщение тарифа.
     */
    @Mapping(source = "tariff.department.id", target = "departmentId")
    @Mapping(source = "tariff.organization.id", target = "organizationId")
    @Mapping(source = "tariff.contract.id", target = "contractId")
    @Mapping(source = "tariff.contract.contractorId", target = "contractorId")
    @Mapping(source = "deleted", target = "deleted")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayMorning", target = "coefWorkDayMorning")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNoon", target = "coefWorkDayNoon")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayEvening", target = "coefWorkDayEvening")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNight", target = "coefWorkDayNight")
    @Mapping(source = "tariff.timedTariffParams.coefDayOff", target = "coefDayOff")
    @Mapping(source = "tariff.coopTariffParams.minCancelTimeMin", target = "minCancelTimeMin", defaultValue = "45")
    @Mapping(source = "tariff.coopTariffParams.savingsDeviationPct", target = "savingsDeviationPct")
    @Mapping(source = "tariff.coopTariffParams.distanceDeviationKm", target = "distanceDeviationKm")
    @Mapping(source = "tariff.coopTariffParams.timeDeviationMin", target = "timeDeviationMin")
    @Mapping(source = "tariff.suburbTariffParams.costPerKmSuburb", target = "costPerKmSuburb")
    @Mapping(source = "tariff.suburbTariffParams.costPerMinSuburb", target = "costPerMinSuburb")
    @Mapping(source = "tariff.suburbTariffParams.suburbServiceCostPerKm", target = "suburbServiceCostPerKm")
    @Mapping(source = "tariff.suburbTariffParams.suburbServiceCostPerMin", target = "suburbServiceCostPerMin")
    @Mapping(source = "tariff.suburbTariffParams.costPerKmInterRegion", target = "costPerKmInterRegion")
    @Mapping(source = "tariff.suburbTariffParams.costPerMinInterRegion", target = "costPerMinInterRegion")
    @Mapping(source = "tariff.contractorDeviationParams.maxDiffComputedDistancePercent", target = "maxDiffComputedDistancePercent")
    @Mapping(source = "tariff.contractorDeviationParams.maxDiffFactDistancePercent", target = "maxDiffFactDistancePercent")
    @Mapping(source = "tariff.contractorDeviationParams.maxDiffComputedCostPercent", target = "maxDiffComputedCostPercent")
    @Mapping(source = "tariff.contractorDeviationParams.maxDiffContractorCostPercent", target = "maxDiffContractorCostPercent")
    @Mapping(source = "tariff.contractorDeviationParams.maxDiffComputedWaitingPercent", target = "maxDiffComputedWaitingPercent")
    @Mapping(source = "tariff.workGroup", target = "workGroup")
    @Mapping(source = "tariff.triggerTime", target = "triggerTime")
    TaxiTariffMessage toMessage(TaxiTariff tariff, boolean deleted);
    
    /**
     * Маппинг тарифа личного транспорта в сообщение.
     *
     * @param tariff тариф.
     * @param deleted флаг удаления.
     *
     * @return сообщение тарифа.
     */
    @Mapping(source = "tariff.department.id", target = "departmentId")
    @Mapping(source = "tariff.organization.id", target = "organizationId")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(source = "tariff.suburbTariffParams.costPerKmSuburb", target = "costPerKmSuburb")
    @Mapping(source = "tariff.suburbTariffParams.costPerMinSuburb", target = "costPerMinSuburb")
    @Mapping(source = "tariff.suburbTariffParams.suburbServiceCostPerKm", target = "suburbServiceCostPerKm")
    @Mapping(source = "tariff.suburbTariffParams.suburbServiceCostPerMin", target = "suburbServiceCostPerMin")
    @Mapping(source = "tariff.suburbTariffParams.costPerKmInterRegion", target = "costPerKmInterRegion")
    @Mapping(source = "tariff.suburbTariffParams.costPerMinInterRegion", target = "costPerMinInterRegion")
    @Mapping(source = "tariff.engineTariffParams.coefEngine1_6", target = "coefEngine1_6")
    @Mapping(source = "tariff.engineTariffParams.coefEngine1_6_to_2_0", target = "coefEngine1_6_to_2_0")
    @Mapping(source = "tariff.engineTariffParams.coefEngine2_0_to_2_5", target = "coefEngine2_0_to_2_5")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayMorning", target = "coefWorkDayMorning")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNoon", target = "coefWorkDayNoon")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayEvening", target = "coefWorkDayEvening")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNight", target = "coefWorkDayNight")
    @Mapping(source = "tariff.timedTariffParams.coefDayOff", target = "coefDayOff")
    @Mapping(source = "tariff.coopTariffParams.minCancelTimeMin", target = "minCancelTimeMin", defaultValue = "45")
    @Mapping(source = "tariff.coopTariffParams.savingsDeviationPct", target = "savingsDeviationPct")
    @Mapping(source = "tariff.coopTariffParams.distanceDeviationKm", target = "distanceDeviationKm")
    @Mapping(source = "tariff.coopTariffParams.timeDeviationMin", target = "timeDeviationMin")
    PersonalTariffMessage toMessage(PersonalTariff tariff, boolean deleted);
    
    /**
     * Маппинг тарифа каршеринга в сообщение.
     *
     * @param tariff тариф.
     * @param deleted флаг удаления.
     *
     * @return сообщение тарифа.
     */
    @Mapping(source = "tariff.department.id", target = "departmentId")
    @Mapping(source = "tariff.organization.id", target = "organizationId")
    @Mapping(source = "tariff.contract.id", target = "contractId")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayMorning", target = "coefWorkDayMorning")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNoon", target = "coefWorkDayNoon")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayEvening", target = "coefWorkDayEvening")
    @Mapping(source = "tariff.timedTariffParams.coefWorkDayNight", target = "coefWorkDayNight")
    @Mapping(source = "tariff.timedTariffParams.coefDayOff", target = "coefDayOff")
    CarSharingTariffMessage toMessage(CarSharingTariff tariff, boolean deleted);
    
    /**
     * Маппинг тарифа общественного транспорта в сообщение.
     *
     * @param tariff тариф.
     * @param deleted флаг удаления.
     *
     * @return сообщение тарифа.
     */
    @Mapping(source = "tariff.department.id", target = "departmentId")
    @Mapping(source = "tariff.organization.id", target = "organizationId")
    @Mapping(source = "deleted", target = "deleted")
    PublicTariffMessage toMessage(PublicTariff tariff, boolean deleted);
    
    @Mapping(source = "tariff.department.id", target = "departmentId")
    @Mapping(source = "tariff.organization.id", target = "organizationId")
    @Mapping(source = "tariff.contract.id", target = "contractId")
    @Mapping(source = "tariff.contract.contractorId", target = "contractorId")
    @Mapping(expression = "java(!tariff.isActive())", target = "deleted")
    GroupTransferTariffMessage toMessage(GroupTransferTariff tariff);
    
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "organizationId", source = "source.organization.id")
    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "regionId", source = "source.regionId")
    @Mapping(target = "transportTypeId", source = "source.transportType.id")
    @Mapping(target = "contractId", expression = "java(contractId(source))")
    @Mapping(target = "workGroup", expression = "java(workGroup(source))")
    @Mapping(target = "priceDetails", source = "priceDetail")
    @Mapping(target = "cloneId", source = "source.id")
    TariffMessage toMessage(BaseTariff source, boolean deleted, Map<String, Object> priceDetail);
    
    default UUID contractId(BaseTariff source) {
        if (source instanceof BaseTariffWithContract withContract) {
            return Optional.ofNullable(withContract.getContract()).map(Contract::getId).orElse(null);
        }
        return null;
    }
    
    default String workGroup(BaseTariff source) {
        if (source instanceof TaxiTariff taxiTariff) {
            return taxiTariff.getWorkGroup();
        }
        return null;
    }
}