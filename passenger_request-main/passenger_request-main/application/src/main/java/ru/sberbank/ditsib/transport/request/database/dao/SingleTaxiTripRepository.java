package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface SingleTaxiTripRepository extends JpaRepository<SingleTaxiTrip, UUID> {
    @Query("select stt from SingleTaxiTrip stt join " +
           " RequestForTaxi  as rq on rq.taxiTrip.id = stt.id where rq.id = :requestId")
    Optional<SingleTaxiTrip> findByRequestId(UUID requestId);

    Optional<SingleTaxiTrip> findByHumanReadableId(String humanReadableId);
    
}
