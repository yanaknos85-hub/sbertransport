package ru.sberbank.ditsib.transport.request.database.dao.approvals.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

public interface PublicTrApprovalsSettingsRepository extends JpaRepository<PublicTrApprovalsSettings, UUID> {
    
    Optional<PublicTrApprovalsSettings> findByOrganizationId(UUID organizationId);
}
