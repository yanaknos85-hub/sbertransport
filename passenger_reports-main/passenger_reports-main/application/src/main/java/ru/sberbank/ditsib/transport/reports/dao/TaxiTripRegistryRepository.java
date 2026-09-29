package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxiTripRegistryRepository extends JpaRepository<TaxiTripRegistry, UUID>,
        JpaSpecificationExecutor<TaxiTripRegistry> {
    
    Optional<TaxiTripRegistry> findByContractorIdAndDate(UUID contractorId, LocalDate date);
    
    List<TaxiTripRegistry> findByContractorIdOrderByDateAsc(UUID contractorId);
}
