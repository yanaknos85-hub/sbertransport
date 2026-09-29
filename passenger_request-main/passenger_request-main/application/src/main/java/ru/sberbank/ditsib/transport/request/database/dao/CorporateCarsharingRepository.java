package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CorporateCarsharingRepository extends JpaRepository<CorporateCarsharing, UUID> {
    
    Optional<CorporateCarsharing> findByContractIdAndOrganizationId(UUID contractId, UUID organizationId);
    List<CorporateCarsharing> findByContractId(UUID contractId);
    List<CorporateCarsharing> findByActive(boolean active);
    Optional<CorporateCarsharing> findByContractContractorIdAndContractRegionAndOrganizationId(
            UUID contractorId, String region, UUID organizationId);
    Optional<CorporateCarsharing> findByContractContractorIdAndContractRegionAndOrganizationIdAndActive(
            UUID contractorId, String region, UUID organizationId, boolean active);
    List<CorporateCarsharing> findByContractRegionAndOrganizationId(String region, UUID organizationId);
    List<CorporateCarsharing> findByOrganizationId(UUID organizationId);
}
