package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий договоров ЭПЛ
 */
@Repository
public interface EwbContractRepository extends JpaRepository<EwbContract, UUID> {
    
    /**
     * Получаем все договора по виду осмотра и идентификатору записи об организации
     *
     * @param inspectionType {@link InspectionType}
     * @param organizationId Идентификатор записи об организации
     *
     * @return {@link List<EwbContract>}
     */
    List<EwbContract> findAllByInspectionTypeInAndOrganizationId(Set<InspectionType> inspectionTypes, UUID organizationId);
}
