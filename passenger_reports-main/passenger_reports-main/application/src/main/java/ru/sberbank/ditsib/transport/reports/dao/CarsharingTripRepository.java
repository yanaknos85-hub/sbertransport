package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sberbank.ditsib.transport.reports.model.CarsharingTrip;
import ru.sberbank.ditsib.transport.reports.model.TransportCompensation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarsharingTripRepository extends JpaRepository<CarsharingTrip, UUID> {
    
    Optional<CarsharingTrip> findFirstByRentId(Integer rentId);
    
    @Query(value = "SELECT car from CarsharingTrip car where car.rentId IN :rentIds")
    List<CarsharingTrip> findAllByRentId(@Param("rentIds") List<Integer> rentIds);
    
}
