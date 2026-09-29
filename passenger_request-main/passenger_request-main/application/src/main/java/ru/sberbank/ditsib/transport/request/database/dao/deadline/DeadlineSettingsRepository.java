package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;

import java.util.Optional;
import java.util.UUID;

public interface DeadlineSettingsRepository extends JpaRepository<DeadlineSettings, UUID> {
   
    Optional<DeadlineSettings> findByOrganizationId(UUID organizationId);
}
