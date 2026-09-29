package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.sberbank.ditsib.transport.tariff.database.model.CarSharingTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

import java.util.UUID;

/**
 * Репозиторий тарифов каршеринга
 */
public interface CarSharingTariffRepository extends TariffRepository<CarSharingTariff> {
}
