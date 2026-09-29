package ru.sberbank.ditsib.transport.request.mappers;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.messaging.*;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.database.model.GroupTransferTariff;
import ru.sberbank.ditsib.transport.request.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.RouteSegmentDTO;

import java.time.Duration;

/**
 * Маппер тарифов.
 */
@Mapper
public interface TariffMapper {
    
    
    TariffDescriptor.Waypoint toWaypoint(Address address);
    
    default TariffDescriptor.NullableString toNullableString(String str) {
        if (str == null) {
            return TariffDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        } else {
            return TariffDescriptor.NullableString.newBuilder().setData(str).build();
        }
    }
    
    RouteSegmentDTO toRouteSegmant(TariffDescriptor.Segment segment);
    
    default Duration map(Long time) {
        return Duration.ofMillis(time);
    }
    
    /**
     * Обновление тарифа общественного транспорта.
     *
     * @param target тариф.
     * @param source сообщение тарифа.
     */
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget PublicTariff target, PublicTariffMessage source);
    
    /**
     * Обновление тарифа личного транспорта.
     *
     * @param target тариф.
     * @param source сообщение тарифа.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "suburbTariffParams.costPerKmSuburb", source = "costPerKmSuburb")
    @Mapping(target = "suburbTariffParams.costPerMinSuburb", source = "costPerMinSuburb")
    @Mapping(target = "suburbTariffParams.suburbServiceCostPerKm", source = "suburbServiceCostPerKm")
    @Mapping(target = "suburbTariffParams.suburbServiceCostPerMin", source = "suburbServiceCostPerMin")
    @Mapping(target = "suburbTariffParams.costPerKmInterRegion", source = "costPerKmInterRegion")
    @Mapping(target = "suburbTariffParams.costPerMinInterRegion", source = "costPerMinInterRegion")
    @Mapping(target = "engineTariffParams.coefEngine1_6", source = "coefEngine1_6")
    @Mapping(target = "engineTariffParams.coefEngine1_6_to_2_0", source = "coefEngine1_6_to_2_0")
    @Mapping(target = "engineTariffParams.coefEngine2_0_to_2_5", source = "coefEngine2_0_to_2_5")
    @Mapping(target = "timedTariffParams.coefWorkDayMorning", source = "coefWorkDayMorning")
    @Mapping(target = "timedTariffParams.coefWorkDayNoon", source = "coefWorkDayNoon")
    @Mapping(target = "timedTariffParams.coefWorkDayEvening", source = "coefWorkDayEvening")
    @Mapping(target = "timedTariffParams.coefWorkDayNight", source = "coefWorkDayNight")
    @Mapping(target = "timedTariffParams.coefDayOff", source = "coefDayOff")
    @Mapping(target = "coopTariffParams.minCancelTimeMin", source = "minCancelTimeMin", defaultValue = "45")
    @Mapping(target = "coopTariffParams.savingsDeviationPct", source = "savingsDeviationPct")
    @Mapping(target = "coopTariffParams.distanceDeviationKm", source = "distanceDeviationKm")
    @Mapping(target = "coopTariffParams.timeDeviationMin", source = "timeDeviationMin")
    void update(@MappingTarget PersonalTariff target, PersonalTariffMessage source);
    
    /**
     * Обновление тарифа каршеринга.
     *
     * @param target тариф.
     * @param source сообщение тарифа.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timedTariffParams.coefWorkDayMorning", source = "coefWorkDayMorning")
    @Mapping(target = "timedTariffParams.coefWorkDayNoon", source = "coefWorkDayNoon")
    @Mapping(target = "timedTariffParams.coefWorkDayEvening", source = "coefWorkDayEvening")
    @Mapping(target = "timedTariffParams.coefWorkDayNight", source = "coefWorkDayNight")
    @Mapping(target = "timedTariffParams.coefDayOff", source = "coefDayOff")
    void update(@MappingTarget CarsharingTariff target, CarSharingTariffMessage source);
    
    /**
     * Обновление тарифа такси.
     *
     * @param target тариф.
     * @param source сообщение тарифа.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timedTariffParams.coefWorkDayMorning", source = "coefWorkDayMorning")
    @Mapping(target = "timedTariffParams.coefWorkDayNoon", source = "coefWorkDayNoon")
    @Mapping(target = "timedTariffParams.coefWorkDayEvening", source = "coefWorkDayEvening")
    @Mapping(target = "timedTariffParams.coefWorkDayNight", source = "coefWorkDayNight")
    @Mapping(target = "timedTariffParams.coefDayOff", source = "coefDayOff")
    @Mapping(target = "coopTariffParams.minCancelTimeMin", source = "minCancelTimeMin", defaultValue = "45")
    @Mapping(target = "coopTariffParams.savingsDeviationPct", source = "savingsDeviationPct")
    @Mapping(target = "coopTariffParams.distanceDeviationKm", source = "distanceDeviationKm")
    @Mapping(target = "coopTariffParams.timeDeviationMin", source = "timeDeviationMin")
    @Mapping(target = "suburbTariffParams.costPerKmSuburb", source = "costPerKmSuburb")
    @Mapping(target = "suburbTariffParams.costPerMinSuburb", source = "costPerMinSuburb")
    @Mapping(target = "suburbTariffParams.suburbServiceCostPerKm", source = "suburbServiceCostPerKm")
    @Mapping(target = "suburbTariffParams.suburbServiceCostPerMin", source = "suburbServiceCostPerMin")
    @Mapping(target = "suburbTariffParams.costPerKmInterRegion", source = "costPerKmInterRegion")
    @Mapping(target = "suburbTariffParams.costPerMinInterRegion", source = "costPerMinInterRegion")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedDistancePercent", source = "maxDiffComputedDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffFactDistancePercent", source = "maxDiffFactDistancePercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedCostPercent", source = "maxDiffComputedCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffContractorCostPercent", source = "maxDiffContractorCostPercent")
    @Mapping(target = "contractorDeviationParams.maxDiffComputedWaitingPercent", source = "maxDiffComputedWaitingPercent")
    void update(@MappingTarget TaxiTariff target, TaxiTariffMessage source);
    
    void map(@MappingTarget GroupTransferTariff target, GroupTransferTariffMessage source);
}
