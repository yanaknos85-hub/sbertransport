package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract repository
 */
@Repository
@Transactional(readOnly = true)
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {
    
    List<Contract> findByContractorIdAndActiveTrue(UUID contractorId);
    
    List<Contract> findByActive(Boolean active);
    
    Optional<Contract> findByContractorIdAndContractNumberAndActiveTrue(UUID contractorId, String contractNumber);
    
    @Query("select c.uvhd from Contract c where c.contractorId = :contractorId and c.uvhd like :uvhd" +
                   " and (:transportType is null or c.transportType = :transportType) group by c.uvhd")
    Page<String> getUniqueUvhd(UUID contractorId, String uvhd, @Param("transportType") TransportTypeEnum transportType, Pageable pageable);
    
    /**
     * Поиск договор по типу договора
     *
     * @param contractType тип договора
     *
     * @return список договоров
     */
    List<Contract> findAllByContractType(ContractType contractType);
}
