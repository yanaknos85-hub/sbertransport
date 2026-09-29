package ru.sberbank.ditsib.transport.tariff.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.tariff.TransportTypeDto;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.dto.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Маппер из сущностей в DTO и обратно
 */
@Mapper
public interface EntityDTOMapper {
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    TaxiTariff newDtoToTaxiTariff(NewTaxiTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    TaxiTariff dtoToTaxiTariff(TaxiTariffDTO source);
    
    @Mapping(source = "id", target = "tariffId")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.contractorId", target = "contractorId")
    @Mapping(source = "contract.contractNumber", target = "contractNumber")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    TaxiTariffDTO taxiTariffToDTO(TaxiTariff source);
    
    @Mapping(source = "id", target = "tariffId", qualifiedByName = "idToTariffId")
    @Mapping(source = "rideCostPerKm", target = "rideCostPerKm", qualifiedByName = "kopToDouble")
    @Mapping(source = "rideCostPerMin", target = "rideCostPerMin", qualifiedByName = "kopToDouble")
    @Mapping(source = "waitCostPerMin", target = "waitCostPerMin", qualifiedByName = "kopToDouble")
    @Mapping(source = "coopTariffParams.minCancelTimeMin", target = "minCancelTimeMin", defaultValue = "45")
    @Mapping(source = "coopTariffParams.savingsDeviationPct", target = "savingsDeviationPct")
    @Mapping(source = "coopTariffParams.distanceDeviationKm", target = "distanceDeviationKm")
    @Mapping(source = "coopTariffParams.timeDeviationMin", target = "timeDeviationMin")
    @Mapping(target = "minRideCost", source = "source", qualifiedByName = "taxiTariffToMinRideCost")
    @Mapping(source = "distanceIncluded", target = "minRideCostKm")
    @Mapping(source = "timeIncluded", target = "minRideCostMin")
    @Mapping(source = "timedTariffParams.coefWorkDayMorning", target = "rateWeekdayMorning")
    @Mapping(source = "timedTariffParams.coefWorkDayNoon", target = "rateWeekdayDay")
    @Mapping(source = "timedTariffParams.coefWorkDayEvening", target = "rateWeekdayEvening")
    @Mapping(source = "timedTariffParams.coefWorkDayNight", target = "rateWeekdayNight")
    @Mapping(source = "timedTariffParams.coefDayOff", target = "rateWeekendSaturday")
    @Mapping(source = "timedTariffParams.coefDayOff", target = "rateWeekendSunday")
    @Mapping(target = "senderService", constant = "TRANSPORT_AS")
    @Mapping(target = "senderTransportType", constant = "TRANSPORT_TAXI")
    @Mapping(target = "deptId", constant = "123")
    @Mapping(target = "vehicleType", constant = "taxi")
    MagentaTaxiTariffDTO taxiTariffToMagentaDTO(TaxiTariff source);
    
    @Mapping(source = "id", target = "tariffId", qualifiedByName = "idToTariffId")
    @Mapping(source = "rideCostPerKm", target = "rideCostPerKm", qualifiedByName = "kopToDouble")
    @Mapping(source = "rideCostPerMin", target = "rideCostPerMin", qualifiedByName = "kopToDouble")
    @Mapping(source = "waitCostPerMin", target = "waitCostPerMin", qualifiedByName = "kopToDouble")
    @Mapping(source = "coopTariffParams.minCancelTimeMin", target = "minCancelTimeMin", defaultValue = "45")
    @Mapping(source = "coopTariffParams.savingsDeviationPct", target = "savingsDeviationPct")
    @Mapping(source = "coopTariffParams.distanceDeviationKm", target = "distanceDeviationKm")
    @Mapping(source = "coopTariffParams.timeDeviationMin", target = "timeDeviationMin")
    @Mapping(target = "minRideCost", source = "source", qualifiedByName = "personalTariffToMinRideCost")
    @Mapping(source = "distanceIncluded", target = "minRideCostKm")
    @Mapping(source = "timeIncluded", target = "minRideCostMin")
    @Mapping(source = "timedTariffParams.coefWorkDayMorning", target = "rateWeekdayMorning")
    @Mapping(source = "timedTariffParams.coefWorkDayNoon", target = "rateWeekdayDay")
    @Mapping(source = "timedTariffParams.coefWorkDayEvening", target = "rateWeekdayEvening")
    @Mapping(source = "timedTariffParams.coefWorkDayNight", target = "rateWeekdayNight")
    @Mapping(source = "timedTariffParams.coefDayOff", target = "rateWeekendSaturday")
    @Mapping(source = "timedTariffParams.coefDayOff", target = "rateWeekendSunday")
    @Mapping(source = "seasonalCoefficient", target = "rateSeason")
    @Mapping(target = "senderService", constant = "TRANSPORT_AS")
    @Mapping(target = "senderTransportType", constant = "TRANSPORT_PERSONAL_CAR")
    @Mapping(target = "deptId", constant = "123")
    @Mapping(target = "vehicleType", constant = "taxi")
    MagentaPersonalTariffDTO personalTariffToMagentaDTO(PersonalTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    @Mapping(target = "distanceIncluded", source = "source", qualifiedByName = "personalDistanceIncluded")
    @Mapping(target = "timeIncluded", source = "source", qualifiedByName = "personalTimeIncluded")
    PersonalTariff newDtoToPersonalTariff(NewPersonalTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    PersonalTariff dtoToPersonalTariff(PersonalTariffDTO source);
    
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    PersonalTariffDTO personalTariffToDTO(PersonalTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    PublicTariff newDtoToPublicTariff(NewPublicTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    PublicTariff dtoToPublicTariff(PublicTariffDTO source);
    
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    PublicTariffDTO publicTariffToDTO(PublicTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    CarSharingTariff newDtoToCarSharingTariff(NewCarSharingTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    CarSharingTariff dtoToCarSharingTariff(CarSharingTariffDTO source);
    
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.contractNumber", target = "contractNumber")
    @Mapping(source = "contract.contractorId", target = "contractorId")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    CarSharingTariffDTO carsharingTariffToDTO(CarSharingTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    BicycleTariff newDtoToBicycleTariff(NewBicycleTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    BicycleTariff dtoToBicycleTariff(BicycleTariffDTO source);
    
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    BicycleTariffDTO bicycleTariffToDTO(BicycleTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    ScooterTariff newDtoToScooterTariff(NewScooterTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    ScooterTariff dtoToScooterTariff(ScooterTariffDTO source);
    
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    ScooterTariffDTO scooterTariffToDTO(ScooterTariff source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    @Mapping(target = "tariffStartDate", expression = "java(source.getTariffStartDate().atTime(java.time.LocalTime.MIN))")
    @Mapping(target = "tariffEndDate", expression = "java(source.getTariffEndDate().atTime(java.time.LocalTime.MAX))")
    GroupTransferTariff newDtoToGroupTransferTariff(NewGroupTransferTariffDTO source);
    
    @Mapping(source = "organizationId", target = "organization.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "regionId", expression = "java(findFirstRegionId(source.getRegionId()))")
    @Mapping(target = "tariffStartDate", expression = "java(source.getTariffStartDate().atTime(java.time.LocalTime.MIN))")
    @Mapping(target = "tariffEndDate", expression = "java(source.getTariffEndDate().atTime(java.time.LocalTime.MAX))")
    GroupTransferTariff dtoToGroupTransferTariff(GroupTransferTariffDTO source);
    
    @Mapping(source = "id", target = "tariffId")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.contractorId", target = "contractorId")
    @Mapping(source = "contract.contractNumber", target = "contractNumber")
    @Mapping(target = "regionId", expression = "java(toSingletonSet(source.getRegionId()))")
    @Mapping(target = "tariffStartDate", expression = "java(source.getTariffStartDate().toLocalDate())")
    @Mapping(target = "tariffEndDate", expression = "java(source.getTariffEndDate().toLocalDate())")
    GroupTransferTariffDTO groupTransferTariffToDTO(GroupTransferTariff source);
    
    default String mapUUID(UUID source) {
        return source == null ? null : source.toString();
    }
    
    default UUID mapTransportType(TransportTypeEnum source) {
        return source == null ? null : source.getId();
    }
    
    default TransportTypeDto mapTransportTypeToDTO(TransportTypeEnum source) {
        return source == null ? null : TransportTypeDto.builder().id(source.getId()).name(source.getName()).build();
    }
    
    //Копейки в double в рублях
    @Named("kopToDouble")
    static Double kopToDouble(Integer price) {
        if (price == null) {
            return null;
        }
        return price.doubleValue() / 100;
    }
    
    //double в рублях в копейки
    @Named("doubleToKop")
    static Integer doubleToKop(Double price) {
        if (price == null) {
            return null;
        }
        return (int) (price * 100);
    }
    
    //UUID в Long для сохранения id в magenta
    @Named("idToTariffId")
    static String idToTariffId(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return "" + (uuid.getMostSignificantBits() & Long.MAX_VALUE);
    }
    
    //минимальная стоимость высчитывается как сумма
    @Named("taxiTariffToMinRideCost")
    static String taxiTariffToMinRideCost(TaxiTariff taxiTariff) {
        return "" + (taxiTariff.getMinRideDistanceCost() + taxiTariff.getMinRideTimeCost()) / 100d;
        
    }
    
    @Named("personalTariffToMinRideCost")
    static String personalTariffToMinRideCost(PersonalTariff personalTariff) {
        int distance = personalTariff.getMinRideDistanceCost();
        int time = personalTariff.getMinRideTimeCost();
        
        return String.valueOf((distance + time) / 100d);
    }
    
    @Named("personalDistanceIncluded")
    static double personalDistanceIncluded(NewPersonalTariffDTO personalTariffDTO) {
        return personalTariffDTO.getMinRideDistanceCost() != null && personalTariffDTO.getRideCostPerKm() > 0 ?
               (double) personalTariffDTO.getMinRideDistanceCost() / personalTariffDTO.getRideCostPerKm() :
               0;
    }
    
    @Named("personalTimeIncluded")
    static int personalTimeIncluded(NewPersonalTariffDTO personalTariffDTO) {
        return personalTariffDTO.getMinRideTimeCost() != null && personalTariffDTO.getRideCostPerMin() > 0 ?
               personalTariffDTO.getMinRideTimeCost() / personalTariffDTO.getRideCostPerMin() :
               0;
    }
    
    default UUID findFirstRegionId(Set<UUID> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) {
            return null;
        }
        
        return regionIds.stream().findFirst().orElse(null);
    }
    
    default Set<UUID> toSingletonSet(UUID value) {
        return Collections.singleton(value);
    }
}