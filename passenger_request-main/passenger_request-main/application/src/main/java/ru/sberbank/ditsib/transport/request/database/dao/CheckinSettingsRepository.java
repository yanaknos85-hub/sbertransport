package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Limit settings repository
 */
@Repository
@Transactional(readOnly = true)
public interface CheckinSettingsRepository extends JpaRepository<CheckinSettings, UUID> {
    
    /**
     * Find CheckinSettings by params.
     *
     * @param serviceType serviceType.
     *
     * @return employee.
     */
    Optional<CheckinSettings> findByServiceTypeAndTransportTypeAndRegion(
            TransportServiceType serviceType,
            TransportTypeEnum transportType,
            UUID region);
}
