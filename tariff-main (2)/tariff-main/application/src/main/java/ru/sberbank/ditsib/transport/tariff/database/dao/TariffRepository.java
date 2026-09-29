package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий тарифов
 */
public interface TariffRepository<T extends BaseTariff> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
    
    Optional<T> findByTransportTypeAndHumanReadableId(TransportTypeEnum transportType, String humanReadableId);
    
}
