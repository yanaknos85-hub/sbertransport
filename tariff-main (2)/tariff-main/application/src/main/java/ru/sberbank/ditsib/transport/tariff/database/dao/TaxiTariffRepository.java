package ru.sberbank.ditsib.transport.tariff.database.dao;

import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий тарифов такси
 */
public interface TaxiTariffRepository extends TariffRepository<TaxiTariff> {
    
    Optional<TaxiTariff> findByDepartmentIdAndTransportTypeAndTaxiClassAndRegionIdAndActive(UUID departmentId, TransportTypeEnum transportType,
                                                                                   TaxiClass taxiClass, UUID regionId, boolean active);
    
    List<TaxiTariff> findAllByDepartmentIdAndTransportType(UUID departmentId, TransportTypeEnum transportType);
}
