package ru.sberbank.ditsib.transport.request.database.dao.approvals.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с настройками согласования всех типов транспорта, кроме такси и общественного
 */
public interface OtherTrTypesApprovalsSettingsRepository extends JpaRepository<OtherTrTypesApprovalsSettings, UUID> {
    
    Optional<OtherTrTypesApprovalsSettings> findByOrganizationIdAndTransportType(UUID organizationId,
                                                                                 TransportTypeEnum transportType);
    
    List<OtherTrTypesApprovalsSettings> findAllByOrganizationId(UUID organizationId);
}
