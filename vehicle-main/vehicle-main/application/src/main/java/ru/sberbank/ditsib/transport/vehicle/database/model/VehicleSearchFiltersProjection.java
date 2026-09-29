package ru.sberbank.ditsib.transport.vehicle.database.model;

import org.springframework.beans.factory.annotation.Value;
import ru.sberbank.ditsib.transport.vehicle.dto.FilterValue;

import java.math.BigDecimal;
import java.util.List;

public interface VehicleSearchFiltersProjection {
    @Value("#{@vehicleFiltersUtil.parseFilterValues(target.bodyType)}")
    List<FilterValue> getBodyType();
    @Value("#{@vehicleFiltersUtil.parseFilterValues(target.engineType)}")
    List<FilterValue> getEngineType();
    @Value("#{@vehicleFiltersUtil.parseFilterValues(target.transmissionType)}")
    List<FilterValue> getTransmissionType();
    @Value("#{@vehicleFiltersUtil.parseFilterValues(target.driveType)}")
    List<FilterValue> getDriveType();
    @Value("#{@vehicleFiltersUtil.parseInts(target.engineCapacity)}")
    List<Integer> getEngineCapacity();
    @Value("#{@vehicleFiltersUtil.parseBigDecimals(target.enginePower)}")
    List<BigDecimal> getEnginePower();
    @Value("#{@vehicleFiltersUtil.parseStrings(target.manufacturePeriod)}")
    List<String> getManufacturePeriod();
}
