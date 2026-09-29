package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff_;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip_;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface CoopTaxiTripRepository extends JpaRepository<CoopTaxiTrip, UUID> {
    @EntityGraph(attributePaths = { CoopTaxiTrip_.SHARED_RIDE, TaxiTrip_.TAXI_ID, TaxiTrip_.TARIFF,
                                    TaxiTrip_.TARIFF + "." + BaseTariff_.CONTRACT,
                                    TaxiTrip_.TARIFF + "." + BaseTariff_.CONTRACT + "." + Contract_.CONTRACTOR })
    @Query("SELECT trip FROM CoopTaxiTrip trip WHERE trip.rideId in (:rideIds)")
    List<CoopTaxiTrip> findAllBySharedRideIds(List<UUID> rideIds);
    
    @Query("SELECT trip FROM CoopTaxiTrip trip WHERE trip.rideId = :rideId")
    Optional<CoopTaxiTrip> findBySharedRideId(UUID rideId);
    
}
