package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Репозиторий тарифов такси
 */
@Repository
public interface TaxiTariffRepository extends JpaRepository<TaxiTariff, UUID> {
    
    @Transactional
    @Modifying
    @Query("update TaxiTariff tt set tt.active = false where tt.id = :id")
    void deactivate(@Param("id") UUID id);
}
