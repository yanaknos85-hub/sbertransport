package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.sberbank.ditsib.transport.tariff.database.model.BicycleTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

import java.util.UUID;

/**
 * Репозиторий тарифов велосипеда
 */
public interface BicycleTariffRepository extends TariffRepository<BicycleTariff> {

}
