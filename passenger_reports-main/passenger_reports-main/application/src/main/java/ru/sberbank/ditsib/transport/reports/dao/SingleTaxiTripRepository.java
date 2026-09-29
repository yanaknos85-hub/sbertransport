package ru.sberbank.ditsib.transport.reports.dao;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff_;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip_;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface SingleTaxiTripRepository extends JpaRepository<SingleTaxiTrip, UUID> {

    @EntityGraph(attributePaths = { SingleTaxiTrip_.REQUEST, TaxiTrip_.TAXI_ID, TaxiTrip_.TARIFF,
                                    TaxiTrip_.TARIFF + "." + BaseTariff_.CONTRACT,
                                    TaxiTrip_.TARIFF + "." + BaseTariff_.CONTRACT + "." + Contract_.CONTRACTOR })
    @Query("SELECT trip FROM SingleTaxiTrip trip INNER JOIN FETCH trip.request request WHERE request.id in (:requestIds)")
    List<SingleTaxiTrip> findAllByRequestIds(@NonNull List<UUID> requestIds);
    
}
