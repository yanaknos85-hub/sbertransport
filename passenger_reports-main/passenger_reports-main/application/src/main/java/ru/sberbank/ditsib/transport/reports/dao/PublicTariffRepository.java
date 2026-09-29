package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.tariff.PublicTariff;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
public interface PublicTariffRepository  extends JpaRepository<PublicTariff, UUID> {

    @Transactional
    @Modifying
    @Query("update PublicTariff tt set tt.active = false where tt.id = :id")
    void deactivate(@Param("id") UUID id);
}
