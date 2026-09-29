package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip_;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface TaxiTripRepository extends JpaRepository<TaxiTrip, UUID> {
    @EntityGraph(attributePaths = { TaxiTrip_.TARIFF })
    Optional<TaxiTrip> findByTaxiId(String taxiId);
}
