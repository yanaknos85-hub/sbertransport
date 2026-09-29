package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    
    @Transactional
    @Modifying
    @Query("update Contract contract set contract.active = false where contract.id = :id")
    void deactivate(@Param("id") UUID id);
    
    @Query("select distinct contractor.id from Contract")
    Set<UUID> findAllContractorIds();
}
