package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.tariff.CarSharingTariff;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Репозиторий тарифов каршеринга
 */
@Repository
public interface CarsharingTariffRepository extends JpaRepository<CarSharingTariff, UUID> {
    
    @Transactional
    @Modifying
    @Query("update CarSharingTariff ct set ct.active = false where ct.id = :id")
    void deactivate(@Param("id") UUID id);
}
