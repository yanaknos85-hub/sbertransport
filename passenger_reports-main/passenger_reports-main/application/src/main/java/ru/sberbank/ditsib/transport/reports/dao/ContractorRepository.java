package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Contractor;

import java.util.Set;
import java.util.UUID;

/**
 * Repository for working with contractors.
 */
@Repository
public interface ContractorRepository extends JpaRepository<Contractor, UUID> {

    @Query( value = "SELECT id FROM Contractor")
    Set<UUID> findContractorsIds();
}
