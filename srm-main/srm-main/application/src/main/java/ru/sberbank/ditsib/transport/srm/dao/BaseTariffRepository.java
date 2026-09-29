package ru.sberbank.ditsib.transport.srm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.srm.model.tariff.BaseTariff;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий тарифов такси
 */
@Repository
public interface BaseTariffRepository extends JpaRepository<BaseTariff, UUID> {
    
    Optional<BaseTariff> findByHumanReadableId(String id);
}
