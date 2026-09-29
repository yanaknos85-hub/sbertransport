package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.TransportCompensation;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransportCompensationRepository extends JpaRepository<TransportCompensation, UUID> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from TransportCompensation")
    void deleteAll();
    
    @Query(value = "SELECT tc from TransportCompensation tc where tc.request.id IN :requestIds")
    List<TransportCompensation> findAllByRequestId(@Param("requestIds") List<UUID> requestIds);
}
