package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxiTripRegistryStringRepository  extends JpaRepository<TaxiTripRegistryString, UUID> {
    
    @Query("select ttrs from TaxiTripRegistryString ttrs where ttrs.parsedString.taxiTripId = :taxiId")
    Optional<TaxiTripRegistryString> findByTaxiId(@Param("taxiId") String taxiId);
}
