package ru.sberbank.ditsib.transport.tariff.service.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.GroupTransferTariff;
import ru.sberbank.ditsib.transport.tariff.dto.files.TransferFileDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Mapper
public abstract class TransferFileDtoMapper {

    private static final String ACTIVE_STATUS = "Активен";
    private static final String NOT_ACTIVE_STATUS = "Неактивен";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tariffStatus", source = "item", qualifiedByName = "mapTariffStatus")
    @Mapping(target = "organization", source = "item.organization.name")
    @Mapping(target = "contract", source = "item.contract.contractNumber")
    @Mapping(target = "contractor", source = "contractor", qualifiedByName = "mapContractor")
    @Mapping(target = "integrationType", source = "contractor", qualifiedByName = "mapIntegrationType")
    @Mapping(target = "active", source = "item.active")
    @Mapping(target = "region", source = "regionName")
    @Mapping(target = "departmentHumanReadableId", ignore = true)
    @Mapping(target = "tariffPeriodFrom", source = "item.tariffStartDate")
    @Mapping(target = "tariffPeriodTo", source = "item.tariffEndDate")
    @Mapping(target = "serviceType", source = "item.transportType.rusName")
    @Mapping(target = "tariffClass", source = "item.groupTransferClass", qualifiedByName = "groupTransferClass")
    @Mapping(target = "minimumTripAttributes.minKm", source = "item.minKm")
    @Mapping(target = "minimumTripAttributes.minRideCost", source = "item.minRideCost")
    @Mapping(target = "minimumTripAttributes.freeWaitingTime", source = "item.freeWaitingTime")
    @Mapping(target = "generalTariffValues.rideCostPerKm", source = "item.rideCostPerKm", qualifiedByName = "centsToRubles")
    @Mapping(target = "generalTariffValues.rideCostPerMin", source = "item.rideCostPerMin", qualifiedByName = "centsToRubles")
    @Mapping(target = "generalTariffValues.waitIntermediateTime", source = "item.waitCostPerMinIntermediate", qualifiedByName = "centsToRubles")
    @Mapping(target = "extendedTariffValues.urb.costPerKm", source = "item.costPerKmCity", qualifiedByName = "centsToRubles")
    @Mapping(target = "extendedTariffValues.urb.costPerMin", source = "item.costPerMinCity", qualifiedByName = "centsToRubles")
    @Mapping(target = "extendedTariffValues.suburb.costPerKm", source = "item.costPerKmSuburb", qualifiedByName = "centsToRubles")
    @Mapping(target = "extendedTariffValues.suburb.costPerMin", source = "item.costPerMinSuburb", qualifiedByName = "centsToRubles")
    @Mapping(target = "extendedTariffValues.waitCostPerMin", source = "item.waitCostPerMin", qualifiedByName = "centsToRubles")
    @Mapping(target = "conditions.minCancelTime", source = "item.minCancelTime")
    @Mapping(target = "conditions.minCreateTime", source = "item.minCreateTime")
    @Mapping(target = "conditions.triggerTime", source = "item.triggerTime")
    public abstract TransferFileDto mapToTransferFileDto(
            @MappingTarget TransferFileDto target, GroupTransferTariff item,
            Contractor contractor, String regionName
    );

    @Named("centsToRubles")
    public double intToDouble(Integer value) {
        return Optional.ofNullable(value).orElse(0) / 100D;
    }

    public double plain(Integer value) {
        return Optional.ofNullable(value).orElse(0);
    }

    public String dateToString(LocalDateTime date) {
        return Optional.ofNullable(date)
                .map(LocalDateTime::toLocalDate)
                .map(LocalDate::toString)
                .orElse(null);
    }

    @Named("groupTransferClass")
    public String groupTransferClass(String source) {
        return Arrays.stream(GroupTransferClass.values())
                .filter(it -> it.name().equals(source))
                .map(GroupTransferClass::getRusName)
                .findFirst()
                .orElse(null);
    }

    @Named("mapTariffStatus")
    protected String mapTariffStatus(GroupTransferTariff item) {
        return item.isActive() ? ACTIVE_STATUS : NOT_ACTIVE_STATUS;
    }

    @Named("mapContractor")
    protected String mapContractor(Contractor contractor) {
        return Optional.ofNullable(contractor).map(Contractor::getName).orElse(null);
    }

    @Named("mapIntegrationType")
    protected String mapIntegrationType(Contractor contractor) {
        return Optional.ofNullable(contractor).map(Contractor::getIntegrationType).orElse(null);
    }
}
