package ru.sberbank.ditsib.transport.srm.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий совместных поездок
 */
@Repository
public interface SrmSharedRideRepository extends JpaRepository<SrmSharedRide, UUID>,
        JpaSpecificationExecutor<SrmSharedRide> {
    
    Page<SrmSharedRide> findByActive(boolean active, Pageable pageable);
    
    List<SrmSharedRide> findByActiveAndTariffIdAndBunchId(boolean active, UUID tariffId, UUID bunchId);

    List<SrmSharedRide> findByBunchIdAndBunchNumber(UUID bunchId, Integer bunchNumber);
}
