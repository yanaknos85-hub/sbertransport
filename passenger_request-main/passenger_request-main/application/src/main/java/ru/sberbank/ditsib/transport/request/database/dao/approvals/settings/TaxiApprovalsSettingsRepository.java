package ru.sberbank.ditsib.transport.request.database.dao.approvals.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

public interface TaxiApprovalsSettingsRepository extends JpaRepository<TaxiApprovalsSettings, UUID> {
    
    Optional<TaxiApprovalsSettings> findByOrganizationId(UUID organizationId);
    
}
