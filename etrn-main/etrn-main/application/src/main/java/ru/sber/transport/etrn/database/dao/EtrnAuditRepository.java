package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.etrn.database.model.EtrnAudit;

import java.util.List;
import java.util.UUID;

@Repository
public interface EtrnAuditRepository extends JpaRepository<EtrnAudit, UUID> {

    List<EtrnAudit> findByEtrnIdOrderByCreatedAtDesc(UUID etrnId);
}